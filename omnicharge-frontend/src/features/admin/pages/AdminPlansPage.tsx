import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Plus, Edit2, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { plansApi, operatorsApi } from '../../../core/api/services';
import type { PlanResponse, OperatorResponse, PageResponse } from '../../../types';
import { PageHeader, Table, ConfirmDialog, Modal, FormField, Spinner, EmptyState, Pagination } from '../../../shared/components';

const schema = z.object({
  planName: z.string().min(2, 'Plan name is required'),
  price: z.string().min(1, 'Price required'),
  validity: z.string().min(1, 'Validity required'),
  data: z.string().optional(),
  calls: z.string().optional(),
  sms: z.string().optional(),
  description: z.string().optional(),
  operatorId: z.string().min(1, 'Select an operator'),
});
type FormData = z.infer<typeof schema>;

const AdminPlansPage: React.FC = () => {
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<PageResponse<PlanResponse> | null>(null);
  const [operators, setOperators] = useState<OperatorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editItem, setEditItem] = useState<PlanResponse | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [filterOp, setFilterOp] = useState<string>('');
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      console.log('Loading plans...');
      const [pRes, oRes] = await Promise.all([plansApi.getAllPaginated(p, 10), operatorsApi.getAll()]);
      console.log('Plans loaded:', pRes.data);
      setPageData(pRes.data);
      setOperators(oRes.data);
    } catch (error) {
      console.error('Failed to load plans:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(page); }, [load, page, refreshTrigger]);

  const openAdd = () => { setEditItem(null); reset({ planName: '', price: '', validity: '', description: '', operatorId: '' }); setModalOpen(true); };
  const openEdit = (p: PlanResponse) => {
    setEditItem(p);
    // Extract days from validity string (e.g., "28 days" -> 28)
    const validityDays = p.validity ? parseInt(p.validity.split(' ')[0], 10) : 28;
    reset({
      planName: p.planName || '',
      price: String(p.amount),
      validity: String(validityDays),
      description: p.description || '',
      operatorId: String(p.operatorId),
    });
    setModalOpen(true);
  };

  const onSubmit = async (data: FormData) => {
    setSaving(true);
    const payload = {
      planName: data.planName,
      amount: parseFloat(data.price), // Backend expects "amount", not "price"
      validity: `${data.validity} days`, // Backend expects validity as string
      description: data.description,
      operatorId: parseInt(data.operatorId, 10),
    };
    console.log('Plan creation payload:', JSON.stringify(payload, null, 2));
    try {
      if (editItem) {
        await plansApi.update(editItem.id, payload);
        toast.success('Plan updated');
      } else {
        const response = await plansApi.create(payload);
        console.log('Plan created successfully:', response);
        toast.success('Plan created');
      }
      setModalOpen(false);
      // Force page refresh by incrementing trigger
      setTimeout(() => { setRefreshTrigger(t => t + 1); }, 300);
    } catch (error: any) {
      console.error('Operation failed:', error);
      console.error('Response status:', error.response?.status);
      console.error('Response data:', error.response?.data);
      console.error('Full error:', JSON.stringify(error.response?.data, null, 2));
      toast.error('Operation failed: ' + (error.response?.data?.message || error.message));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setDeleteLoading(true);
    try {
      await plansApi.delete(deleteId);
      toast.success('Plan deleted');
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

  const filtered = useMemo(() => {
    const plans = pageData?.content ?? [];
    return filterOp ? plans.filter((p) => p.operatorId === Number(filterOp)) : plans;
  }, [pageData, filterOp]);

  const cols = [
    { key: 'name', label: 'Plan' },
    { key: 'op', label: 'Operator' },
    { key: 'price', label: 'Price' },
    { key: 'validity', label: 'Validity' },
    { key: 'actions', label: 'Actions' },
  ];

  return (
    <div className="animate-fade-in space-y-6">
      <PageHeader
        title="Plans"
        subtitle={pageData ? `${pageData.page?.totalElements || 0} plans` : ''}
        action={
          <button onClick={openAdd} className="btn-primary flex items-center gap-2 text-sm md:text-base">
            <Plus size={16} /> Add Plan
          </button>
        }
      />

      <div>
        <select
          value={filterOp}
          onChange={(e) => setFilterOp(e.target.value)}
          className="input-field w-full md:max-w-xs text-sm md:text-base"
        >
          <option value="">All Operators</option>
          {operators.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
        </select>
      </div>

      {/* Desktop Table View */}
      <div className="hidden md:block overflow-x-auto">
        {!loading && filtered.length === 0 ? (
          <div className="card"><EmptyState title="No plans found" icon="" /></div>
        ) : (
          <Table columns={cols} loading={loading}>
            {filtered.map((p) => (
              <tr key={p.id} className="table-row">
                <td className="px-4 py-3">
                  <p className="text-white font-medium text-sm">{p.planName}</p>
                </td>
                <td className="px-4 py-3 text-slate-200 text-sm">
                  {p.operatorName || operators.find(o => o.id === p.operatorId)?.name || `#${p.operatorId}`}
                </td>
                <td className="px-4 py-3 text-white font-bold">₹{p.amount}</td>
                <td className="px-4 py-3 text-slate-200 text-sm">{p.validity}</td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => openEdit(p)}
                      className="p-1.5 rounded-lg text-slate-200 hover:text-brand hover:bg-brand/10 transition-colors"
                    >
                      <Edit2 size={14} />
                    </button>
                    <button
                      onClick={() => setDeleteId(p.id)}
                      className="p-1.5 rounded-lg text-slate-200 hover:text-accent-red hover:bg-accent-red/10 transition-colors"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        )}
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
          <EmptyState title="No plans found" icon="" />
        ) : (
          filtered.map((p) => (
            <div key={p.id} className="p-4 rounded-lg border border-surface-border/50 bg-transparent space-y-3">
              {/* Header with plan name and operator */}
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <p className="text-white font-medium text-sm line-clamp-1">{p.planName}</p>
                  <p className="text-slate-300 text-xs">
                    {p.operatorName || operators.find(o => o.id === p.operatorId)?.name || `#${p.operatorId}`}
                  </p>
                </div>
              </div>

              {/* Price and Validity */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <p className="text-slate-300 text-xs mb-1">Price</p>
                  <p className="text-white font-bold text-lg">₹{p.amount}</p>
                </div>
                <div>
                  <p className="text-slate-300 text-xs mb-1">Validity</p>
                  <p className="text-slate-300 text-sm font-medium">{p.validity}</p>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex gap-2 pt-2">
                <button
                  onClick={() => openEdit(p)}
                  className="flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-sm font-medium text-brand bg-brand/10 hover:bg-brand/20 transition-colors"
                >
                  <Edit2 size={14} />
                  Edit
                </button>
                <button
                  onClick={() => setDeleteId(p.id)}
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editItem ? 'Edit Plan' : 'Add Plan'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormField label="Plan Name" error={errors.planName?.message}>
              <input {...register('planName')} className="input-field text-sm" placeholder="e.g. Basic Plan" />
            </FormField>
            <FormField label="Operator" error={errors.operatorId?.message}>
              <select {...register('operatorId')} className="input-field text-sm">
                <option value="">Select operator…</option>
                {operators.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
              </select>
            </FormField>
            <FormField label="Price (₹)" error={errors.price?.message}>
              <input {...register('price')} type="number" step="0.01" className="input-field text-sm" placeholder="299" />
            </FormField>
            <FormField label="Validity (days)" error={errors.validity?.message}>
              <input {...register('validity')} type="number" className="input-field text-sm" placeholder="28" />
            </FormField>
          </div>
          <FormField label="Description (optional)">
            <textarea {...register('description')} rows={2} className="input-field resize-none text-sm" placeholder="Plan benefits and details…" />
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
        title="Delete Plan"
        message="This plan will be permanently deleted. Users with active recharges on this plan may be affected."
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

export default AdminPlansPage;
