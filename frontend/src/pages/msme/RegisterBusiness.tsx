import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useNavigate } from 'react-router-dom';
import { registerBusiness } from '../../services/businessService';
import { useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Building2, FileCheck2, MapPin, ArrowRight, ShieldCheck, AlertTriangle } from 'lucide-react';

const businessSchema = z.object({
  legalName: z.string().min(2, 'Legal name must be at least 2 characters'),
  tradeName: z.string().optional(),
  gstin: z.string().regex(/^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/, 'Invalid GSTIN format (e.g. 27AAAAA1111A1Z1)'),
  pan: z.string().regex(/^[A-Z]{5}[0-9]{4}[A-Z]{1}$/, 'Invalid PAN format (e.g. ABCDE1234F)'),
  udyamNumber: z.string().min(1, 'Udyam Registration Number is required'),
  incorporationDate: z.string().min(1, 'Incorporation date is required'),
  constitution: z.string().min(1, 'Please select constitution type'),
  industrySector: z.string().min(1, 'Please select industry sector'),
  addressLine1: z.string().min(1, 'Address line 1 is required'),
  addressLine2: z.string().optional(),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(1, 'State is required'),
  pincode: z.string().regex(/^[0-9]{6}$/, 'Pincode must be exactly 6 digits'),
});

type BusinessFormValues = z.infer<typeof businessSchema>;

export const RegisterBusiness = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<BusinessFormValues>({
    resolver: zodResolver(businessSchema),
  });

  const onSubmit = async (data: BusinessFormValues) => {
    setIsLoading(true);
    setErrorMsg(null);
    setSuccessMsg(null);
    try {
      await registerBusiness(data);
      queryClient.invalidateQueries({ queryKey: ['my-business'] });
      setSuccessMsg('Business profile registered successfully! Opening dashboard...');
      setTimeout(() => {
        navigate('/msme');
      }, 2000);
    } catch (err: any) {
      const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response.data : null) || err.message || String(err);
      setErrorMsg(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const prefillDemoData = () => {
    setValue('legalName', 'Saraswati Textiles Private Limited');
    setValue('tradeName', 'Saraswati Fabrics');
    setValue('gstin', '27AAAAA1111A1Z1');
    setValue('pan', 'AAAAA1111A');
    setValue('udyamNumber', 'UDYAM-MH-12-0004561');
    setValue('incorporationDate', '2018-05-20');
    setValue('constitution', 'Private Limited Company');
    setValue('industrySector', 'Manufacturing (Textiles)');
    setValue('addressLine1', 'Plot No 45, MIDC Industrial Area');
    setValue('addressLine2', 'Opposite Power Grid Station');
    setValue('city', 'Nagpur');
    setValue('state', 'Maharashtra');
    setValue('pincode', '440012');
  };

  return (
    <div className="min-h-screen bg-slate-50 py-8 px-4 sm:px-6 lg:px-8 flex flex-col justify-center">
      <div className="max-w-4xl mx-auto w-full">
        {/* Top Header */}
        <div className="flex items-center space-x-3 mb-8 justify-center sm:justify-start">
          <div className="bg-primary h-10 w-10 rounded-xl flex items-center justify-center shadow-md">
            <span className="font-extrabold text-accent text-xl">IDBI</span>
          </div>
          <div>
            <h1 className="text-xl font-extrabold tracking-tight text-primary m-0">MSME Platform</h1>
            <p className="text-[10px] text-slate-500 font-semibold uppercase tracking-wider m-0">Corporate Node Setup</p>
          </div>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-3xl shadow-xl border border-slate-100 overflow-hidden"
        >
          {/* Top Banner */}
          <div className="bg-primary-900 px-8 py-6 text-white flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-primary-850">
            <div>
              <h2 className="text-2xl font-bold text-white m-0">Register Your Business</h2>
              <p className="text-slate-300 text-xs mt-1">Provide corporate details to configure your alternate lending twin.</p>
            </div>
            <button
              type="button"
              onClick={prefillDemoData}
              className="mt-4 sm:mt-0 px-4 py-2 bg-accent hover:bg-yellow-600 text-primary-900 font-bold text-xs rounded-xl shadow transition duration-200"
            >
              Prefill Nagpur Factory (Demo)
            </button>
          </div>

          <form className="p-8 space-y-8" onSubmit={handleSubmit(onSubmit)}>
            {/* Feedback Alerts */}
            {errorMsg && (
              <div className="p-4 rounded-xl bg-red-50 border border-red-100 flex items-start space-x-3 text-red-700 text-sm">
                <AlertTriangle className="h-5 w-5 shrink-0 text-red-500 mt-0.5" />
                <div>
                  <span className="font-semibold block">Registration Error</span>
                  <span>{errorMsg}</span>
                </div>
              </div>
            )}

            {successMsg && (
              <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-100 flex items-start space-x-3 text-emerald-800 text-sm">
                <ShieldCheck className="h-5 w-5 shrink-0 text-emerald-600 mt-0.5" />
                <div>
                  <span className="font-semibold block">Success</span>
                  <span>{successMsg}</span>
                </div>
              </div>
            )}

            {/* Section 1: Entity Details */}
            <div>
              <div className="flex items-center space-x-2 pb-3 mb-6 border-b border-slate-100 text-primary">
                <Building2 size={20} />
                <h3 className="text-md font-bold text-slate-800 uppercase tracking-wider">Business Entity Profile</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Legal Registered Name</label>
                  <input
                    type="text"
                    placeholder="E.g. Saraswati Fabrics Pvt Ltd"
                    {...register('legalName')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                  />
                  {errors.legalName && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.legalName.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Trade Name (Optional)</label>
                  <input
                    type="text"
                    placeholder="E.g. Saraswati Fabrics"
                    {...register('tradeName')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Constitution of Business</label>
                  <select
                    {...register('constitution')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                  >
                    <option value="">-- Select Constitution --</option>
                    <option value="Proprietorship">Sole Proprietorship</option>
                    <option value="Partnership">Partnership Firm</option>
                    <option value="LLP">Limited Liability Partnership (LLP)</option>
                    <option value="Private Limited Company">Private Limited Company</option>
                    <option value="Public Limited Company">Public Limited Company</option>
                  </select>
                  {errors.constitution && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.constitution.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Industry Sector</label>
                  <select
                    {...register('industrySector')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                  >
                    <option value="">-- Select Sector --</option>
                    <option value="Manufacturing (Textiles)">Manufacturing (Textiles)</option>
                    <option value="Manufacturing (Others)">Manufacturing (Others)</option>
                    <option value="Services (Logistics)">Services (Logistics)</option>
                    <option value="Services (IT)">Services (IT)</option>
                    <option value="Wholesale Trade">Wholesale Trade</option>
                    <option value="Retail Trade">Retail Trade</option>
                  </select>
                  {errors.industrySector && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.industrySector.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Date of Incorporation</label>
                  <input
                    type="date"
                    {...register('incorporationDate')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                  />
                  {errors.incorporationDate && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.incorporationDate.message}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Section 2: Registration Details */}
            <div>
              <div className="flex items-center space-x-2 pb-3 mb-6 border-b border-slate-100 text-primary">
                <FileCheck2 size={20} />
                <h3 className="text-md font-bold text-slate-800 uppercase tracking-wider">Tax & Regulatory IDs</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">GSTIN (15-char ID)</label>
                  <input
                    type="text"
                    placeholder="27AAAAA1111A1Z1"
                    {...register('gstin')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition uppercase"
                  />
                  {errors.gstin && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.gstin.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Company PAN (10-char ID)</label>
                  <input
                    type="text"
                    placeholder="ABCDE1234F"
                    {...register('pan')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition uppercase"
                  />
                  {errors.pan && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.pan.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Udyam Registration No.</label>
                  <input
                    type="text"
                    placeholder="UDYAM-XX-00-0000000"
                    {...register('udyamNumber')}
                    className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                  />
                  {errors.udyamNumber && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{errors.udyamNumber.message}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Section 3: Address Details */}
            <div>
              <div className="flex items-center space-x-2 pb-3 mb-6 border-b border-slate-100 text-primary">
                <MapPin size={20} />
                <h3 className="text-md font-bold text-slate-800 uppercase tracking-wider">Registered Corporate Address</h3>
              </div>

              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Address Line 1</label>
                    <input
                      type="text"
                      placeholder="Plot No., Building Name, Street"
                      {...register('addressLine1')}
                      className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                    />
                    {errors.addressLine1 && (
                      <p className="mt-1 text-xs text-red-600 font-medium">{errors.addressLine1.message}</p>
                    )}
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Address Line 2 (Optional)</label>
                    <input
                      type="text"
                      placeholder="Locality, Landmark"
                      {...register('addressLine2')}
                      className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">City</label>
                    <input
                      type="text"
                      placeholder="City"
                      {...register('city')}
                      className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                    />
                    {errors.city && (
                      <p className="mt-1 text-xs text-red-600 font-medium">{errors.city.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">State</label>
                    <input
                      type="text"
                      placeholder="State"
                      {...register('state')}
                      className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                    />
                    {errors.state && (
                      <p className="mt-1 text-xs text-red-600 font-medium">{errors.state.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Pincode (6 digits)</label>
                    <input
                      type="text"
                      placeholder="400001"
                      {...register('pincode')}
                      className="block w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition"
                    />
                    {errors.pincode && (
                      <p className="mt-1 text-xs text-red-600 font-medium">{errors.pincode.message}</p>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {/* Submission Actions */}
            <div className="flex items-center justify-end space-x-4 pt-6 border-t border-slate-100">
              <button
                type="button"
                onClick={() => navigate('/msme')}
                className="px-6 py-3 border border-slate-250 rounded-xl text-slate-750 font-bold text-sm hover:bg-slate-50 transition"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isLoading}
                className="flex items-center space-x-2 px-8 py-3 bg-primary text-white rounded-xl shadow-md font-bold text-sm hover:bg-primary-800 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                {isLoading ? (
                  <div className="h-5 w-5 animate-spin rounded-full border-2 border-solid border-white border-t-transparent"></div>
                ) : (
                  <>
                    <span>Register Business Node</span>
                    <ArrowRight size={16} />
                  </>
                )}
              </button>
            </div>
          </form>
        </motion.div>
      </div>
    </div>
  );
};
export default RegisterBusiness;
