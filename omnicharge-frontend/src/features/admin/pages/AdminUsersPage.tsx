import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Search, Trash2, Shield, User } from 'lucide-react';
import { usersApi } from '../../../core/api/services';
import type { UserResponse, Role, PageResponse } from '../../../types';
import { PageHeader, Table, StatusBadge, ConfirmDialog, Modal, FormField, Spinner, Pagination } from '../../../shared/components';
import { useDebounce } from '../../../shared/hooks/useOptimizations';
import toast from 'react-hot-toast';

const AdminUsersPage: React.FC = () => {
  const [page, setPage]                = useState(0);
  const [pageData, setPageData]        = useState<PageResponse<UserResponse> | null>(null);
  const [search, setSearch]            = useState('');
  const [loading, setLoading]          = useState(true);
  const [deleteId, setDeleteId]        = useState<number | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [roleModal, setRoleModal]      = useState<UserResponse | null>(null);
  const [newRole, setNewRole]          = useState<Role>('USER');
  const [roleLoading, setRoleLoading]  = useState(false);

  // OPTIMIZATION: debounce search — avoids filtering on every keystroke
  const debouncedSearch = useDebounce(search, 300);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const { data } = await usersApi.getAll(p, 10);
      setPageData(data);
    } catch {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(page); }, [load, page]);

  // OPTIMIZATION: client-side filter on current page with debounce
  const filtered = useMemo(() => {
    const q = debouncedSearch.toLowerCase();
    if (!q || !pageData) return pageData?.content ?? [];
    return pageData.content.filter((u) =>
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.contactNo?.includes(q)
    );
  }, [pageData, debouncedSearch]);

  const handleDelete = async () => {
    if (!deleteId) return;
    setDeleteLoading(true);
    try {
      await usersApi.deleteUser(deleteId);
      toast.success('User deleted');
      setDeleteId(null);
      load(page);
    } catch {
      toast.error('Delete failed');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleRoleUpdate = async () => {
    if (!roleModal) return;
    setRoleLoading(true);
    try {
      await usersApi.updateRole(roleModal.userId, { role: newRole });
      toast.success('Role updated');
      setRoleModal(null);
      load(page);
    } catch {
      toast.error('Role update failed');
    } finally {
      setRoleLoading(false);
    }
  };

  const cols = [
    { key: 'id',      label: '#' },
    { key: 'name',    label: 'Name' },
    { key: 'email',   label: 'Email' },
    { key: 'phone',   label: 'Phone' },
    { key: 'role',    label: 'Role' },
    { key: 'actions', label: 'Actions' },
  ];

  return (
    <div className="animate-fade-in space-y-6">
      <PageHeader title="Users" subtitle={pageData ? `${pageData.page?.totalElements || 0} total users` : ''} />

      <div className="relative w-full md:max-w-sm">
        <Search size={16} className="absolute left-3 md:left-4 top-1/2 -translate-y-1/2 text-slate-200" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, or phone…"
          className="input-field pl-10 md:pl-11 text-sm md:text-base"
        />
      </div>

      {/* Desktop Table View */}
      <div className="hidden md:block overflow-x-auto">
        <Table columns={cols} loading={loading}>
          {filtered.length === 0 ? (
            <tr>
              <td colSpan={6} className="py-12 text-center text-slate-300 text-sm">No users found</td>
            </tr>
          ) : filtered.map((u) => (
            <tr key={u.userId} className="table-row">
              <td className="px-4 py-3 text-slate-300 font-mono text-xs">#{u.userId}</td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-brand/15 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0">
                    {u.name?.split(' ')[0]?.[0]?.toUpperCase()}
                  </div>
                  <p className="text-white font-medium text-sm">{u.name}</p>
                </div>
              </td>
              <td className="px-4 py-3 text-slate-200 text-sm line-clamp-1">{u.email}</td>
              <td className="px-4 py-3 text-slate-200 text-sm">{u.contactNo || '—'}</td>
              <td className="px-4 py-3"><StatusBadge status={u.role} /></td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => { setRoleModal(u); setNewRole(u.role); }}
                    className="p-1.5 rounded-lg text-slate-200 hover:text-accent-amber hover:bg-accent-amber/10 transition-colors"
                    title="Change role"
                  >
                    <Shield size={15} />
                  </button>
                  <button
                    onClick={() => setDeleteId(u.userId)}
                    className="p-1.5 rounded-lg text-slate-200 hover:text-accent-red hover:bg-accent-red/10 transition-colors"
                    title="Delete user"
                  >
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
          <div className="py-8 text-center text-slate-300 text-sm">No users found</div>
        ) : (
          filtered.map((u) => (
            <div key={u.userId} className="p-4 rounded-lg border border-surface-border/50 bg-transparent space-y-3">
              {/* Header with avatar and name */}
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-8 h-8 rounded-full bg-brand/15 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0">
                    {u.name?.split(' ')[0]?.[0]?.toUpperCase()}
                  </div>
                  <div className="min-w-0">
                    <p className="text-white font-medium text-sm line-clamp-1">{u.name}</p>
                    <p className="text-slate-300 text-xs">ID: #{u.userId}</p>
                  </div>
                </div>
                <StatusBadge status={u.role} />
              </div>

              {/* Email and Phone */}
              <div className="space-y-2 text-sm">
                <div>
                  <p className="text-slate-300 text-xs">Email</p>
                  <p className="text-slate-300 text-xs line-clamp-1">{u.email}</p>
                </div>
                {u.contactNo && (
                  <div>
                    <p className="text-slate-300 text-xs">Phone</p>
                    <p className="text-slate-300 text-xs">{u.contactNo}</p>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              <div className="flex gap-2 pt-2">
                <button
                  onClick={() => { setRoleModal(u); setNewRole(u.role); }}
                  className="flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-sm font-medium text-accent-amber bg-accent-amber/10 hover:bg-accent-amber/20 transition-colors"
                >
                  <Shield size={14} />
                  Change Role
                </button>
                <button
                  onClick={() => setDeleteId(u.userId)}
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

      <ConfirmDialog
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        title="Delete User"
        message="This action cannot be undone. The user and all associated data will be permanently removed."
        loading={deleteLoading}
      />

      <Modal isOpen={!!roleModal} onClose={() => setRoleModal(null)} title="Update User Role" size="sm">
        {roleModal && (
          <div className="space-y-4">
            <p className="text-slate-200 text-xs md:text-sm">
              Changing role for <strong className="text-white">{roleModal.name}</strong>
            </p>
            <FormField label="New Role">
              <div className="space-y-2">
                {(['USER', 'ADMIN'] as Role[]).map((r) => (
                  <button
                    key={r}
                    type="button"
                    onClick={() => setNewRole(r)}
                    className={`w-full flex items-center gap-2 md:gap-3 p-2 md:p-3 rounded-lg md:rounded-xl border transition-all ${
                      newRole === r
                        ? 'border-brand/40 bg-brand/10 text-white'
                        : 'border-surface-border text-slate-200 hover:border-slate-500'
                    }`}
                  >
                    {r === 'ADMIN'
                      ? <Shield size={16} className="text-accent-amber" />
                      : <User   size={16} className="text-brand" />}
                    <span className="font-medium text-sm">{r}</span>
                  </button>
                ))}
              </div>
            </FormField>
            <div className="flex flex-col-reverse sm:flex-row gap-2 md:gap-3 pt-2">
              <button onClick={() => setRoleModal(null)} className="btn-ghost w-full sm:w-auto text-sm">Cancel</button>
              <button onClick={handleRoleUpdate} disabled={roleLoading} className="btn-primary w-full sm:w-auto flex items-center justify-center gap-2 text-sm">
                {roleLoading && <Spinner size={14} />} Update Role
              </button>
            </div>
          </div>
        )}
      </Modal>

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

export default AdminUsersPage;
