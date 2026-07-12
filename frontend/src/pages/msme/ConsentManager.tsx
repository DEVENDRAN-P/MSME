import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMyAllConsents, getMyPendingConsents, updateConsentStatus } from '../../services/consentService';
import { motion, AnimatePresence } from 'framer-motion';
import {
  ShieldAlert,
  CheckCircle,
  XCircle,
  FileCheck,
  Smartphone,
  ShieldCheck,
  UserCheck,
  ArrowLeft,
  Calendar,
  Lock,
  Unlock
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ConsentManager = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // Queries
  const { data: pendingConsents, isLoading: isPendingLoading } = useQuery({
    queryKey: ['my-pending-consents'],
    queryFn: getMyPendingConsents,
  });

  const { data: allConsents, isLoading: isAllLoading } = useQuery({
    queryKey: ['my-all-consents'],
    queryFn: getMyAllConsents,
  });

  // Mutations
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateConsentStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-pending-consents'] });
      queryClient.invalidateQueries({ queryKey: ['my-all-consents'] });
    },
  });

  const handleUpdate = (id: string, status: 'APPROVED' | 'DENIED' | 'REVOKED') => {
    updateStatusMutation.mutate({ id, status });
  };

  const isLoading = isPendingLoading || isAllLoading;

  const getConsentTypeIcon = (type: string) => {
    switch (type) {
      case 'GST':
        return <FileCheck className="text-blue-600" size={18} />;
      case 'UPI':
        return <Smartphone className="text-emerald-600" size={18} />;
      default:
        return <ShieldCheck className="text-primary-650" size={18} />;
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-8">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="flex items-center space-x-4 mb-8">
          <button
            onClick={() => navigate('/msme')}
            className="p-2 bg-white hover:bg-slate-100 rounded-xl border border-slate-200 transition text-slate-600"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="text-3xl font-black text-slate-900 tracking-tight m-0">Data Consent Manager</h1>
            <p className="text-slate-500 text-sm mt-1">Control which financial trails are shared with loan underwriting officers.</p>
          </div>
        </div>

        {isLoading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-4 border-solid border-primary border-t-transparent"></div>
          </div>
        ) : (
          <div className="space-y-10">
            {/* Section 1: Pending Requests */}
            <div>
              <div className="flex items-center space-x-2 text-primary-950 mb-6 pb-2 border-b border-slate-200">
                <ShieldAlert size={20} />
                <h3 className="text-sm font-black uppercase tracking-wider">Pending Access Requests</h3>
              </div>

              {pendingConsents && pendingConsents.length === 0 ? (
                <div className="bg-white rounded-2xl border border-slate-100 p-8 text-center text-slate-500 text-xs font-semibold shadow-sm">
                  <Unlock className="mx-auto text-slate-350 mb-3" size={24} />
                  <span>No pending data access requests at this moment.</span>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <AnimatePresence>
                    {pendingConsents?.map((consent) => (
                      <motion.div
                        key={consent.id}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        className="bg-white rounded-2xl border border-slate-150 p-6 shadow-sm flex flex-col justify-between"
                      >
                        <div>
                          <div className="flex items-center justify-between mb-4">
                            <span className="text-[10px] uppercase font-bold tracking-widest text-slate-400">Request ID: {consent.id.slice(0, 8)}</span>
                            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-100">
                              PENDING APPROVAL
                            </span>
                          </div>
                          <h4 className="font-extrabold text-slate-800 text-sm flex items-center space-x-1.5">
                            <UserCheck size={16} className="text-primary-650" />
                            <span>{consent.requestedByName}</span>
                          </h4>
                          <p className="text-[10px] text-slate-400 font-semibold mt-1">Requested Data Access: <strong className="text-slate-600">{consent.consentType}</strong></p>
                          <p className="text-xs text-slate-500 mt-4 leading-normal">
                            This officer requests access to view your business profile registry and alternate transactional cash flows for loan underwriting review.
                          </p>
                        </div>
                        <div className="flex gap-4 mt-6 border-t border-slate-100 pt-4">
                          <button
                            onClick={() => handleUpdate(consent.id, 'APPROVED')}
                            className="flex-grow py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-md transition flex items-center justify-center space-x-1.5"
                          >
                            <CheckCircle size={14} />
                            <span>Approve Access</span>
                          </button>
                          <button
                            onClick={() => handleUpdate(consent.id, 'DENIED')}
                            className="flex-grow py-2 bg-white hover:bg-rose-50 text-rose-600 border border-rose-250 font-bold text-xs rounded-xl transition flex items-center justify-center space-x-1.5"
                          >
                            <XCircle size={14} />
                            <span>Deny</span>
                          </button>
                        </div>
                      </motion.div>
                    ))}
                  </AnimatePresence>
                </div>
              )}
            </div>

            {/* Section 2: Active & Historical Consents */}
            <div>
              <div className="flex items-center space-x-2 text-primary-950 mb-6 pb-2 border-b border-slate-200">
                <Lock size={20} />
                <h3 className="text-sm font-black uppercase tracking-wider">Consent Registries History</h3>
              </div>

              {allConsents && allConsents.length === 0 ? (
                <div className="bg-white rounded-2xl border border-slate-100 p-8 text-center text-slate-500 text-xs font-semibold shadow-sm">
                  <span>No consent histories logged in the registry.</span>
                </div>
              ) : (
                <div className="bg-white rounded-2xl border border-slate-150 overflow-hidden shadow-sm">
                  <table className="min-w-full text-left text-sm text-slate-500">
                    <thead className="bg-slate-50 text-slate-700 text-xs font-bold uppercase tracking-wider">
                      <tr>
                        <th className="px-6 py-4">Lender / Officer</th>
                        <th className="px-6 py-4">Data Type</th>
                        <th className="px-6 py-4">Validity</th>
                        <th className="px-6 py-4">Status</th>
                        <th className="px-6 py-4 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {allConsents?.map((consent) => (
                        <tr key={consent.id} className="hover:bg-slate-50/50">
                          <td className="px-6 py-4">
                            <div className="font-semibold text-slate-900">{consent.requestedByName}</div>
                            <div className="text-[10px] text-slate-400">ID: {consent.id.slice(0, 8)}</div>
                          </td>
                          <td className="px-6 py-4">
                            <div className="flex items-center space-x-2 text-slate-800 font-medium">
                              {getConsentTypeIcon(consent.consentType)}
                              <span>{consent.consentType} trails</span>
                            </div>
                          </td>
                          <td className="px-6 py-4 text-xs text-slate-600">
                            {consent.status === 'APPROVED' ? (
                              <div className="flex items-center space-x-1">
                                <Calendar size={14} className="text-slate-400" />
                                <span>Until: {new Date(consent.validUntil).toLocaleDateString()}</span>
                              </div>
                            ) : (
                              <span>—</span>
                            )}
                          </td>
                          <td className="px-6 py-4">
                            <span className={`px-2.5 py-1 text-[10px] font-black rounded-md border uppercase tracking-wider ${
                              consent.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' :
                              consent.status === 'PENDING' ? 'bg-amber-50 text-amber-700 border-amber-100' :
                              'bg-slate-100 text-slate-500 border-slate-200'
                            }`}>
                              {consent.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-right">
                            {consent.status === 'APPROVED' && (
                              <button
                                onClick={() => handleUpdate(consent.id, 'REVOKED')}
                                className="px-3 py-1.5 bg-rose-50 border border-rose-250 hover:bg-rose-100 text-rose-600 font-bold text-xs rounded-lg transition"
                              >
                                Revoke Access
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ConsentManager;
