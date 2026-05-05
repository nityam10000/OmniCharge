import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Plus, Edit2, Trash2, Search } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { operatorsApi } from '../../../core/api/services';
import type { OperatorResponse, PageResponse } from '../../../types';
import { PageHeader, Table, ConfirmDialog, Modal, FormField, Spinner, EmptyState, Pagination } from '../../../shared/components';
import { useAppContext } from '../../../core/context/AppContext';
import { useDebounce } from '../../../shared/hooks/useOptimizations';

const schema = z.object({
  name:        z.string().min(2, 'Name is required'),
  description: z.string().optional(),
  logoUrl:     z.string().url('Must be a valid URL').optional().or(z.literal('')),
});
type FormData = z.infer<typeof schema>;

const AdminOperatorsPage: React.FC = () => {
  const { invalidateOperators, invalidatePlans } = useAppContext();

  const [page, setPage]            = useState(0);
  const [pageData, setPageData]    = useState<PageResponse<OperatorResponse> | null>(null);
  const [loading, setLoading]      = useState(true);
  const [search, setSearch]        = useState('');
  const [modalOpen, setModalOpen]  = useState(false);
  const [editItem, setEditItem]    = useState<OperatorResponse | null>(null);
  const [saving, setSaving]        = useState(false);
  const [deleteId, setDeleteId]    = useState<number | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const debouncedSearch = useDebounce(search, 300);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      console.log('Loading operators...');
      const { data } = await operatorsApi.getAllPaginated(p, 10);
      console.log('Operators loaded:', data);
      setPageData(data);
    } catch (error) {
      console.error('Failed to load operators:', error);
      toast.error('Failed to load operators');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(page); }, [load, page, refreshTrigger]);

  const filtered = useMemo(() => {
    const q = debouncedSearch.toLowerCase();
    const operators = pageData?.content ?? [];
    if (!q) return operators;
    return operators.filter((o) =>
      o.name.toLowerCase().includes(q) ||
      o.description?.toLowerCase().includes(q)
    );
  }, [pageData, debouncedSearch]);

  const openAdd  = () => { setEditItem(null); reset({ name: '', description: '', logoUrl: '' }); setModalOpen(true); };
  const openEdit = (op: OperatorResponse) => {
    setEditItem(op);
    reset({ name: op.name, description: op.description || '', logoUrl: op.logoUrl || '' });
    setModalOpen(true);
  };

  const onSubmit = async (data: FormData) => {
    setSaving(true);
    try {
      if (editItem) {
        await operatorsApi.update(editItem.id, data);
        toast.success('Operator updated');
        invalidatePlans(editItem.id);
      } else {
        await operatorsApi.create(data);
        toast.success('Operator created');
      }
      invalidateOperators(); // clear operator cache
      setModalOpen(false);
      // Force page refresh by incrementing trigger
      setTimeout(() => { setRefreshTrigger(t => t + 1); }, 300);
    } catch (error) {
      console.error('Save failed:', error);
      toast.error('Save failed');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setDeleteLoading(true);
    try {
      await operatorsApi.delete(deleteId);
      toast.success('Operator deleted');
      invalidateOperators();
      invalidatePlans(deleteId);
      setDeleteId(null);
      // Force page refresh by incrementing trigger
      setTimeout(() => { setRefreshTrigger(t => t + 1); }, 300);
    } catch (error) {
      console.error('Delete failed:', error);
      toast.error('Delete failed');
    } finally {
      setDeleteLoading(false);
    }
  };

  const cols = [
    { key: 'id',      label: '#' },
    { key: 'name',    label: 'Operator' },
    { key: 'desc',    label: 'Description' },
    { key: 'plans',   label: 'Plans' },
    { key: 'actions', label: 'Actions' },
  ];

  return (
    <div className="animate-fade-in space-y-6">
      <PageHeader
        title="Operators"
        subtitle={pageData ? `${pageData.page?.totalElements || 0} operators` : ''}
        action={
          <button onClick={openAdd} className="btn-primary flex items-center gap-2 text-sm md:text-base">
            <Plus size={15} /> Add Operator
          </button>
        }
      />

      <div className="relative w-full md:max-w-sm">
        <Search size={16} className="absolute left-3 md:left-4 top-1/2 -translate-y-1/2 text-slate-300" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search operators…"
          className="input-field pl-10 md:pl-11 text-sm md:text-base"
        />
      </div>

      {/* Desktop Table View */}
      <div className="hidden md:block overflow-x-auto">
        <Table columns={cols} loading={loading}>
          {filtered.length === 0 ? (
            <tr><td colSpan={5} className="py-12"><EmptyState title="No operators found" /></td></tr>
          ) : filtered.map((op) => (
            <tr key={op.id} className="table-row">
              <td className="px-4 py-3 text-slate-300 font-mono text-xs">#{op.id}</td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-3">
                  {op.logoUrl
                    ? <img src={op.logoUrl} alt={op.name} className="w-8 h-8 rounded-lg object-contain" loading="lazy" />
                    : <div className="w-8 h-8 rounded-lg bg-brand/10 flex items-center justify-center text-xs font-bold text-brand">
                        {op.name.slice(0, 2).toUpperCase()}
                      </div>
                  }
                  <span className="text-white font-medium text-sm">{op.name}</span>
                </div>
              </td>
              <td className="px-4 py-3 text-slate-200 text-sm line-clamp-1">{op.description || '—'}</td>
              <td className="px-4 py-3 text-slate-200 text-sm">{op.planCount ?? '—'}</td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  <button onClick={() => openEdit(op)} className="p-1.5 rounded-lg text-slate-200 hover:text-brand hover:bg-brand/10 transition-colors" title="Edit">
                    <Edit2 size={15} />
                  </button>
                  <button onClick={() => setDeleteId(op.id)} className="p-1.5 rounded-lg text-slate-200 hover:text-accent-red hover:bg-accent-red/10 transition-colors" title="Delete">
                    <Trash2 size={15} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </Table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden space-y-3">
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="p-4 rounded-lg border border-surface-border/50 bg-transparent space-y-2">
              <div className="h-4 bg-surface-border/30 rounded w-1/3" />
              <div className="h-3 bg-surface-border/30 rounded w-2/3" />
            </div>
          ))
        ) : filtered.length === 0 ? (
          <EmptyState title="No operators found" />
        ) : (
          filtered.map((op) => (
            <div key={op.id} className="p-4 rounded-lg border border-surface-border/50 bg-transparent space-y-3">
              {/* Header with logo and name */}
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  {op.logoUrl
                    ? <img src={op.logoUrl} alt={op.name} className="w-8 h-8 rounded-lg object-contain flex-shrink-0" loading="lazy" />
                    : <div className="w-8 h-8 rounded-lg bg-brand/10 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0">
                        {op.name.slice(0, 2).toUpperCase()}
                      </div>
                  }
                  <div className="min-w-0">
                    <p className="text-white font-medium text-sm line-clamp-1">{op.name}</p>
                    <p className="text-slate-300 text-xs">ID: #{op.id}</p>
                  </div>
                </div>
                <div className="text-right flex-shrink-0">
                  <p className="text-slate-300 text-xs">Plans</p>
                  <p className="text-brand font-semibold text-sm">{op.planCount ?? '—'}</p>
                </div>
              </div>

              {/* Description */}
              {op.description && (
                <div>
                  <p className="text-slate-300 text-xs mb-1">Description</p>
                  <p className="text-slate-300 text-xs line-clamp-2">{op.description}</p>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex gap-2 pt-2">
                <button
                  onClick={() => openEdit(op)}
                  className="flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-sm font-medium text-brand bg-brand/10 hover:bg-brand/20 transition-colors"
                >
                  <Edit2 size={14} />
                  Edit
                </button>
                <button
                  onClick={() => setDeleteId(op.id)}
                  className="flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-sm font-medium text-accent-red bg-accent-red/10 hover:bg-accent-red/20 transition-colors"
                >
                  <Trash2 size={14} />
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editItem ? 'Edit Operator' : 'Add Operator'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <FormField label="Name" error={errors.name?.message}>
            <input {...register('name')} className="input-field text-sm" placeholder="e.g. Jio" />
          </FormField>
          <FormField label="Description" error={errors.description?.message}>
            <input {...register('description')} className="input-field text-sm" placeholder="Short description" />
          </FormField>
          <FormField label="Logo URL" error={errors.logoUrl?.message}>
            <input {...register('logoUrl')} className="input-field text-sm" placeholder="https://…" />
          </FormField>
          <div className="flex flex-col-reverse sm:flex-row gap-2 md:gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="btn-ghost w-full sm:w-auto text-sm">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary w-full sm:w-auto flex items-center justify-center gap-2 text-sm">
              {saving && <Spinner size={14} />} {editItem ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        title="Delete Operator"
        message="This will permanently delete the operator and all its plans."
        loading={deleteLoading}
      />

      {pageData && (
        <Pagination
          page={page}
          totalPages={pageData.page?.totalPages || 1}
          onPageChange={(p) => { setPage(p); }}
        />
      )}
    </div>
  );
};

export default AdminOperatorsPage;
