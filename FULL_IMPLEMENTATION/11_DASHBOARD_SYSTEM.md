# MODULE 11: DASHBOARD SYSTEM
## Production-Grade Implementation (4 Dashboards)

---

## 11.1 FRONTEND ARCHITECTURE

```
frontend/src/
├── pages/
│   ├── dashboards/
│   │   ├── MSMEDashboard.tsx
│   │   ├── LoanOfficerDashboard.tsx
│   │   ├── CreditManagerDashboard.tsx
│   │   └── AdminDashboard.tsx
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   └── RegisterPage.tsx
│   ├── msme/
│   │   ├── ProfilePage.tsx
│   │   ├── HealthCardPage.tsx
│   │   ├── LoanApplicationPage.tsx
│   │   └── ReportsPage.tsx
│   └── shared/
│       ├── NotFoundPage.tsx
│       └── UnauthorizedPage.tsx
├── components/
│   ├── dashboards/
│   │   ├── DashboardLayout.tsx
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── StatsCard.tsx
│   ├── charts/
│   │   ├── RadarChart.tsx
│   │   ├── HealthGauge.tsx
│   │   ├── TrendLine.tsx
│   │   └── RiskHeatmap.tsx
│   ├── ui/
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   ├── Table.tsx
│   │   ├── Badge.tsx
│   │   ├── Modal.tsx
│   │   └── LoadingSpinner.tsx
│   └── shared/
│       ├── NotificationBell.tsx
│       └── UserMenu.tsx
├── hooks/
│   ├── useAuth.ts
│   ├── useNotifications.ts
│   └── useWebSocket.ts
├── api/
│   ├── client.ts
│   ├── auth.api.ts
│   ├── msme.api.ts
│   ├── health.api.ts
│   ├── forecast.api.ts
│   └── notification.api.ts
├── store/
│   ├── authStore.ts
│   └── uiStore.ts
├── types/
│   └── index.ts
└── utils/
    ├── formatters.ts
    └── validators.ts
```

---

## 11.2 MSME DASHBOARD

```tsx
// frontend/src/pages/dashboards/MSMEDashboard.tsx
import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { RadarChart, HealthGauge, TrendLine } from '@/components/charts';
import { useAuth } from '@/hooks/useAuth';
import { healthApi } from '@/api/health.api';
import { msmeApi } from '@/api/msme.api';

interface MSMEDashboardData {
  profile: {
    businessName: string;
    industry: string;
    employeeCount: number;
    annualTurnover: number;
  };
  healthScore: {
    overall: number;
    grade: string;
    dimensions: Record<string, number>;
    trend: { date: string; score: number }[];
    confidence: number;
  };
  loanStatus: {
    applications: any[];
    activeLoans: any[];
  };
  quickStats: {
    pendingInvoices: number;
    upcomingEmis: number;
    alertsCount: number;
  };
}

const MSMEDashboard: React.FC = () => {
  const { user } = useAuth();
  
  const { data: dashboardData, isLoading } = useQuery<MSMEDashboardData>({
    queryKey: ['msmeDashboard'],
    queryFn: async () => {
      const [health, profile, loans] = await Promise.all([
        healthApi.getLatestScore(user?.msmeId),
        msmeApi.getProfile(user?.msmeId),
        msmeApi.getLoans(user?.msmeId)
      ]);
      
      return {
        profile: profile.data,
        healthScore: health.data,
        loanStatus: loans.data,
        quickStats: {
          pendingInvoices: 12,
          upcomingEmis: 3,
          alertsCount: 2
        }
      };
    }
  });
  
  if (isLoading) {
    return <DashboardSkeleton />;
  }
  
  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">
              Welcome, {dashboardData?.profile.businessName}
            </h1>
            <p className="text-gray-500">
              {dashboardData?.profile.industry} • {dashboardData?.profile.employeeCount} employees
            </p>
          </div>
          <Badge 
            variant={
              dashboardData?.healthScore.grade.startsWith('A') ? 'success' :
              dashboardData?.healthScore.grade.startsWith('B') ? 'warning' : 'destructive'
            }
          >
            Grade: {dashboardData?.healthScore.grade}
          </Badge>
        </div>
        
        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatsCard
            title="Financial Health"
            value={`${dashboardData?.healthScore.overall}/100`}
            icon="📊"
            trend={+5}
          />
          <StatsCard
            title="Pending Invoices"
            value={dashboardData?.quickStats.pendingInvoices}
            icon="📄"
          />
          <StatsCard
            title="Active Loans"
            value={dashboardData?.loanStatus.activeLoans?.length || 0}
            icon="🏦"
          />
          <StatsCard
            title="Alerts"
            value={dashboardData?.quickStats.alertsCount}
            icon="⚠️"
            variant={dashboardData?.quickStats.alertsCount > 0 ? 'warning' : 'default'}
          />
        </div>
        
        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Health Score Card */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Health Score Trend</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <TrendLine data={dashboardData?.healthScore.trend || []} />
                </div>
                <div className="ml-4">
                  <HealthGauge 
                    score={dashboardData?.healthScore.overall} 
                    grade={dashboardData?.healthScore.grade}
                    size={150}
                  />
                </div>
              </div>
              <div className="mt-4 text-sm text-gray-500">
                Confidence: {(dashboardData?.healthScore.confidence || 0) * 100}%
              </div>
            </CardContent>
          </Card>
          
          {/* Dimension Breakdown */}
          <Card>
            <CardHeader>
              <CardTitle>Dimension Scores</CardTitle>
            </CardHeader>
            <CardContent>
              <RadarChart scores={dashboardData?.healthScore.dimensions || {}} />
            </CardContent>
          </Card>
        </div>
        
        {/* Bottom Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Loan Applications */}
          <Card>
            <CardHeader>
              <CardTitle>Recent Loan Applications</CardTitle>
            </CardHeader>
            <CardContent>
              <LoanApplicationsList 
                applications={dashboardData?.loanStatus.applications || []} 
              />
            </CardContent>
          </Card>
          
          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent>
              <QuickActions />
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
};

const StatsCard: React.FC<{
  title: string;
  value: string | number;
  icon: string;
  trend?: number;
  variant?: 'default' | 'warning' | 'success' | 'destructive';
}> = ({ title, value, icon, trend, variant = 'default' }) => (
  <Card className={variant !== 'default' ? `border-${variant}` : ''}>
    <CardContent className="p-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-gray-500">{title}</p>
          <p className="text-2xl font-bold">{value}</p>
          {trend && (
            <p className={`text-sm ${trend > 0 ? 'text-green-600' : 'text-red-600'}`}>
              {trend > 0 ? '↑' : '↓'} {Math.abs(trend)}%
            </p>
          )}
        </div>
        <span className="text-3xl">{icon}</span>
      </div>
    </CardContent>
  </Card>
);

const QuickActions: React.FC = () => (
  <div className="grid grid-cols-2 gap-4">
    <button className="p-4 border rounded-lg hover:bg-gray-50 text-left">
      <span className="text-2xl">📊</span>
      <p className="font-medium mt-2">View Health Card</p>
      <p className="text-sm text-gray-500">Download detailed report</p>
    </button>
    <button className="p-4 border rounded-lg hover:bg-gray-50 text-left">
      <span className="text-2xl">💰</span>
      <p className="font-medium mt-2">Apply for Loan</p>
      <p className="text-sm text-gray-500">Quick loan application</p>
    </button>
    <button className="p-4 border rounded-lg hover:bg-gray-50 text-left">
      <span className="text-2xl">📈</span>
      <p className="font-medium mt-2">Run Simulation</p>
      <p className="text-sm text-gray-500">What-if scenarios</p>
    </button>
    <button className="p-4 border rounded-lg hover:bg-gray-50 text-left">
      <span className="text-2xl">📋</span>
      <p className="font-medium mt-2">View Reports</p>
      <p className="text-sm text-gray-500">All generated reports</p>
    </button>
  </div>
);

const LoanApplicationsList: React.FC<{ applications: any[] }> = ({ applications }) => (
  <div className="space-y-3">
    {applications.length === 0 ? (
      <p className="text-gray-500 text-center py-4">No loan applications</p>
    ) : (
      applications.slice(0, 5).map((app) => (
        <div key={app.id} className="flex items-center justify-between p-3 bg-gray-50 rounded">
          <div>
            <p className="font-medium">₹{app.amount.toLocaleString()}</p>
            <p className="text-sm text-gray-500">{app.type} • {app.date}</p>
          </div>
          <Badge variant={
            app.status === 'approved' ? 'success' :
            app.status === 'pending' ? 'warning' : 'destructive'
          }>
            {app.status}
          </Badge>
        </div>
      ))
    )}
  </div>
);

const DashboardSkeleton: React.FC = () => (
  <div className="p-6 space-y-6 animate-pulse">
    <div className="h-8 bg-gray-200 rounded w-1/3" />
    <div className="grid grid-cols-4 gap-4">
      {[...Array(4)].map((_, i) => (
        <div key={i} className="h-24 bg-gray-200 rounded" />
      ))}
    </div>
    <div className="grid grid-cols-3 gap-6">
      <div className="col-span-2 h-64 bg-gray-200 rounded" />
      <div className="h-64 bg-gray-200 rounded" />
    </div>
  </div>
);

export default MSMEDashboard;
```

---

## 11.3 LOAN OFFICER DASHBOARD

```tsx
// frontend/src/pages/dashboards/LoanOfficerDashboard.tsx
import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Modal } from '@/components/ui/Modal';
import { healthApi } from '@/api/health.api';
import { lendingApi } from '@/api/lending.api';

interface LoanApplication {
  id: string;
  msmeName: string;
  msmeId: string;
  amount: number;
  type: string;
  status: 'pending' | 'under_review' | 'approved' | 'rejected';
  riskScore: number;
  healthScore: number;
  submittedAt: string;
  documentsComplete: boolean;
}

const LoanOfficerDashboard: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedApp, setSelectedApp] = useState<LoanApplication | null>(null);
  const [filters, setFilters] = useState({
    status: 'all',
    search: '',
    riskLevel: 'all'
  });
  
  const { data: applications, isLoading } = useQuery<LoanApplication[]>({
    queryKey: ['loanApplications', filters],
    queryFn: async () => {
      const response = await lendingApi.getApplications(filters);
      return response.data;
    }
  });
  
  const approveMutation = useMutation({
    mutationFn: async (applicationId: string) => {
      await lendingApi.approveApplication(applicationId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loanApplications'] });
      setSelectedApp(null);
    }
  });
  
  const rejectMutation = useMutation({
    mutationFn: async ({ applicationId, reason }: { applicationId: string; reason: string }) => {
      await lendingApi.rejectApplication(applicationId, reason);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loanApplications'] });
      setSelectedApp(null);
    }
  });
  
  const filteredApplications = applications?.filter(app => {
    if (filters.status !== 'all' && app.status !== filters.status) return false;
    if (filters.search && !app.msmeName.toLowerCase().includes(filters.search.toLowerCase())) return false;
    return true;
  }) || [];
  
  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold">Loan Officer Dashboard</h1>
          <div className="flex gap-2">
            <Badge variant="outline">Queue: {filteredApplications.length}</Badge>
            <Badge variant="success">
              {filteredApplications.filter(a => a.status === 'approved').length} approved today
            </Badge>
          </div>
        </div>
        
        {/* Queue Summary */}
        <div className="grid grid-cols-4 gap-4">
          <QueueCard title="Pending Review" count={12} color="yellow" />
          <QueueCard title="Under Analysis" count={5} color="blue" />
          <QueueCard title="Approved Today" count={3} color="green" />
          <QueueCard title="Rejected Today" count={1} color="red" />
        </div>
        
        {/* Filters */}
        <Card>
          <CardContent className="p-4">
            <div className="flex gap-4">
              <Input 
                placeholder="Search by MSME name or ID..."
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                className="w-64"
              />
              <select 
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                className="border rounded px-3 py-2"
              >
                <option value="all">All Status</option>
                <option value="pending">Pending</option>
                <option value="under_review">Under Review</option>
              </select>
              <select 
                value={filters.riskLevel}
                onChange={(e) => setFilters({ ...filters, riskLevel: e.target.value })}
                className="border rounded px-3 py-2"
              >
                <option value="all">All Risk Levels</option>
                <option value="low">Low Risk</option>
                <option value="medium">Medium Risk</option>
                <option value="high">High Risk</option>
              </select>
            </div>
          </CardContent>
        </Card>
        
        {/* Applications Table */}
        <Card>
          <CardHeader>
            <CardTitle>Loan Applications Queue</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>MSME Name</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Health Score</TableHead>
                  <TableHead>Risk Level</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredApplications.map((app) => (
                  <TableRow key={app.id}>
                    <TableCell>
                      <div>
                        <p className="font-medium">{app.msmeName}</p>
                        <p className="text-sm text-gray-500">{app.msmeId}</p>
                      </div>
                    </TableCell>
                    <TableCell>₹{app.amount.toLocaleString()}</TableCell>
                    <TableCell>{app.type}</TableCell>
                    <TableCell>
                      <ScoreBadge score={app.healthScore} />
                    </TableCell>
                    <TableCell>
                      <RiskBadge score={app.riskScore} />
                    </TableCell>
                    <TableCell>
                      <Badge variant={getStatusVariant(app.status)}>
                        {app.status.replace('_', ' ')}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Button size="sm" onClick={() => setSelectedApp(app)}>
                        Review
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
        
        {/* Review Panel */}
        {selectedApp && (
          <ReviewPanel
            application={selectedApp}
            onClose={() => setSelectedApp(null)}
            onApprove={() => approveMutation.mutate(selectedApp.id)}
            onReject={(reason) => rejectMutation.mutate({ 
              applicationId: selectedApp.id, 
              reason 
            })}
            isApproving={approveMutation.isPending}
            isRejecting={rejectMutation.isPending}
          />
        )}
      </div>
    </DashboardLayout>
  );
};

const QueueCard: React.FC<{
  title: string;
  count: number;
  color: string;
}> = ({ title, count, color }) => (
  <Card>
    <CardContent className="p-4">
      <div className="flex items-center gap-3">
        <div className={`w-3 h-3 rounded-full bg-${color}-500`} />
        <div>
          <p className="text-sm text-gray-500">{title}</p>
          <p className="text-2xl font-bold">{count}</p>
        </div>
      </div>
    </CardContent>
  </Card>
);

const ScoreBadge: React.FC<{ score: number }> = ({ score }) => (
  <div className="flex items-center gap-2">
    <div className="w-12 bg-gray-200 rounded-full h-2">
      <div 
        className={`h-2 rounded-full ${
          score >= 70 ? 'bg-green-500' : score >= 50 ? 'bg-yellow-500' : 'bg-red-500'
        }`}
        style={{ width: `${score}%` }}
      />
    </div>
    <span className="text-sm font-medium">{score}</span>
  </div>
);

const RiskBadge: React.FC<{ score: number }> = ({ score }) => {
  const level = score < 25 ? 'Low' : score < 50 ? 'Medium' : score < 75 ? 'High' : 'Very High';
  return (
    <Badge variant={
      score < 25 ? 'success' : score < 50 ? 'warning' : 'destructive'
    }>
      {level}
    </Badge>
  );
};

const ReviewPanel: React.FC<{
  application: LoanApplication;
  onClose: () => void;
  onApprove: () => void;
  onReject: (reason: string) => void;
  isApproving: boolean;
  isRejecting: boolean;
}> = ({ application, onClose, onApprove, onReject, isApproving, isRejecting }) => {
  const [rejectionReason, setRejectionReason] = useState('');
  
  return (
    <Modal onClose={onClose} title="Review Application">
      <div className="space-y-6">
        {/* MSME Info */}
        <div>
          <h3 className="font-semibold mb-2">MSME Information</h3>
          <div className="bg-gray-50 p-4 rounded space-y-2">
            <p><span className="text-gray-500">Name:</span> {application.msmeName}</p>
            <p><span className="text-gray-500">Amount:</span> ₹{application.amount.toLocaleString()}</p>
            <p><span className="text-gray-500">Type:</span> {application.type}</p>
          </div>
        </div>
        
        {/* AI Recommendation */}
        <div>
          <h3 className="font-semibold mb-2">AI Recommendation</h3>
          <div className="bg-blue-50 p-4 rounded">
            <p className="text-blue-800">
              Based on health score ({application.healthScore}) and risk assessment 
              ({application.riskScore}), we recommend {application.riskScore < 50 ? 'approval' : 'careful review'}.
            </p>
          </div>
        </div>
        
        {/* Actions */}
        <div className="space-y-3 pt-4 border-t">
          <Button 
            className="w-full" 
            onClick={onApprove}
            disabled={isApproving}
          >
            {isApproving ? 'Approving...' : 'Approve Application'}
          </Button>
          <textarea
            placeholder="Rejection reason (required)"
            value={rejectionReason}
            onChange={(e) => setRejectionReason(e.target.value)}
            className="w-full border rounded p-2"
            rows={3}
          />
          <Button 
            variant="destructive" 
            className="w-full"
            disabled={!rejectionReason || isRejecting}
            onClick={() => onReject(rejectionReason)}
          >
            {isRejecting ? 'Rejecting...' : 'Reject Application'}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

const getStatusVariant = (status: string) => {
  switch (status) {
    case 'approved': return 'success';
    case 'pending': return 'warning';
    case 'rejected': return 'destructive';
    default: return 'secondary';
  }
};

export default LoanOfficerDashboard;
```

---

## 11.4 CREDIT MANAGER DASHBOARD

```tsx
// frontend/src/pages/dashboards/CreditManagerDashboard.tsx
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { RiskHeatmap, IndustryComparison } from '@/components/charts';
import { portfolioApi } from '@/api/portfolio.api';

const CreditManagerDashboard: React.FC = () => {
  const { data: portfolio } = useQuery({
    queryKey: ['portfolio'],
    queryFn: async () => {
      const response = await portfolioApi.getOverview();
      return response.data;
    }
  });
  
  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <h1 className="text-2xl font-bold">Credit Manager Dashboard</h1>
        
        {/* Portfolio Metrics */}
        <div className="grid grid-cols-4 gap-4">
          <MetricCard title="Total Portfolio" value="₹45.2 Cr" change="+12%" />
          <MetricCard title="Active Loans" value="156" change="+8" />
          <MetricCard title="NPA Rate" value="2.3%" change="-0.5%" />
          <MetricCard title="Avg Risk Score" value="42" change="+3" />
        </div>
        
        <Tabs defaultValue="portfolio">
          <TabsList>
            <TabsTrigger value="portfolio">Portfolio Analysis</TabsTrigger>
            <TabsTrigger value="risk">Risk Monitoring</TabsTrigger>
            <TabsTrigger value="benchmarks">Industry Benchmarks</TabsTrigger>
          </TabsList>
          
          <TabsContent value="portfolio" className="space-y-6">
            <div className="grid grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Portfolio by Industry</CardTitle>
                </CardHeader>
                <CardContent>
                  <IndustryDistributionChart />
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <CardTitle>Portfolio by Risk Level</CardTitle>
                </CardHeader>
                <CardContent>
                  <RiskDistributionChart />
                </CardContent>
              </Card>
            </div>
            
            <Card>
              <CardHeader>
                <CardTitle>High Growth MSMEs</CardTitle>
              </CardHeader>
              <CardContent>
                <HighGrowthMSMEsList />
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="risk" className="space-y-6">
            <div className="grid grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Risk Heatmap</CardTitle>
                </CardHeader>
                <CardContent>
                  <RiskHeatmap risks={{
                    'Credit Risk': 35,
                    'Market Risk': 28,
                    'Operational Risk': 42,
                    'Liquidity Risk': 22,
                    'Concentration Risk': 55
                  }} />
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <CardTitle>Early Warning Signals</CardTitle>
                </CardHeader>
                <CardContent>
                  <EarlyWarningList />
                </CardContent>
              </Card>
            </div>
            
            <Card>
              <CardHeader>
                <CardTitle>High Risk MSMEs</CardTitle>
              </CardHeader>
              <CardContent>
                <HighRiskMSMEsList />
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="benchmarks" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Industry Comparison</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <IndustryComparison 
                    dimension="Overall Score"
                    msmeScore={72}
                    industryAvg={65}
                    p25={50}
                    p75={82}
                  />
                  <IndustryComparison 
                    dimension="Cash Flow"
                    msmeScore={68}
                    industryAvg={62}
                    p25={45}
                    p75={78}
                  />
                  <IndustryComparison 
                    dimension="Compliance"
                    msmeScore={85}
                    industryAvg={72}
                    p25={58}
                    p75={88}
                  />
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

const MetricCard: React.FC<{
  title: string;
  value: string;
  change: string;
}> = ({ title, value, change }) => (
  <Card>
    <CardContent className="p-4">
      <p className="text-sm text-gray-500">{title}</p>
      <p className="text-2xl font-bold">{value}</p>
      <p className={`text-sm ${change.startsWith('+') ? 'text-green-600' : 'text-red-600'}`}>
        {change}
      </p>
    </CardContent>
  </Card>
);

const IndustryDistributionChart: React.FC = () => (
  <div className="h-64 flex items-center justify-center bg-gray-50 rounded">
    <p className="text-gray-500">Industry Distribution Chart</p>
  </div>
);

const RiskDistributionChart: React.FC = () => (
  <div className="h-64 flex items-center justify-center bg-gray-50 rounded">
    <p className="text-gray-500">Risk Distribution Chart</p>
  </div>
);

const HighGrowthMSMEsList: React.FC = () => (
  <div className="space-y-3">
    {[1, 2, 3].map((i) => (
      <div key={i} className="flex items-center justify-between p-3 bg-gray-50 rounded">
        <div>
          <p className="font-medium">MSME Business {i}</p>
          <p className="text-sm text-gray-500">Growth: +{(20 + i * 5)}%</p>
        </div>
        <p className="font-bold text-green-600">Score: {85 + i}</p>
      </div>
    ))}
  </div>
);

const EarlyWarningList: React.FC = () => (
  <div className="space-y-3">
    <div className="p-3 bg-yellow-50 rounded border-l-4 border-yellow-500">
      <p className="font-medium text-yellow-800">Revenue Decline</p>
      <p className="text-sm text-yellow-600">3 MSMEs showing 15%+ decline</p>
    </div>
    <div className="p-3 bg-red-50 rounded border-l-4 border-red-500">
      <p className="font-medium text-red-800">Cash Flow Stress</p>
      <p className="text-sm text-red-600">2 MSMEs with &lt;3 months runway</p>
    </div>
  </div>
);

const HighRiskMSMEsList: React.FC = () => (
  <div className="space-y-3">
    {[1, 2].map((i) => (
      <div key={i} className="flex items-center justify-between p-3 bg-red-50 rounded">
        <div>
          <p className="font-medium">High Risk MSME {i}</p>
          <p className="text-sm text-gray-500">Risk Score: {75 + i * 5}</p>
        </div>
        <Badge variant="destructive">Review Required</Badge>
      </div>
    ))}
  </div>
);

export default CreditManagerDashboard;
```

---

## 11.5 ADMIN DASHBOARD

```tsx
// frontend/src/pages/dashboards/AdminDashboard.tsx
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { adminApi } from '@/api/admin.api';

const AdminDashboard: React.FC = () => {
  const { data: systemHealth } = useQuery({
    queryKey: ['systemHealth'],
    queryFn: async () => {
      const response = await adminApi.getSystemHealth();
      return response.data;
    }
  });
  
  const { data: userStats } = useQuery({
    queryKey: ['userStats'],
    queryFn: async () => {
      const response = await adminApi.getUserStats();
      return response.data;
    }
  });
  
  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <h1 className="text-2xl font-bold">Admin Dashboard</h1>
        
        {/* System Health */}
        <div className="grid grid-cols-4 gap-4">
          <SystemHealthCard 
            title="API Response" 
            value="45ms" 
            status="healthy" 
          />
          <SystemHealthCard 
            title="DB Connections" 
            value="24/100" 
            status="healthy" 
          />
          <SystemHealthCard 
            title="Cache Hit Rate" 
            value="94%" 
            status="healthy" 
          />
          <SystemHealthCard 
            title="Error Rate" 
            value="0.02%" 
            status="healthy" 
          />
        </div>
        
        <Tabs defaultValue="users">
          <TabsList>
            <TabsTrigger value="users">User Management</TabsTrigger>
            <TabsTrigger value="ai">AI Monitoring</TabsTrigger>
            <TabsTrigger value="system">System Health</TabsTrigger>
            <TabsTrigger value="audit">Audit Logs</TabsTrigger>
          </TabsList>
          
          <TabsContent value="users" className="space-y-6">
            <div className="grid grid-cols-3 gap-4">
              <Card>
                <CardContent className="p-4">
                  <p className="text-sm text-gray-500">Total Users</p>
                  <p className="text-2xl font-bold">{userStats?.total || 0}</p>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <p className="text-sm text-gray-500">Active MSMEs</p>
                  <p className="text-2xl font-bold">{userStats?.activeMsme || 0}</p>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <p className="text-sm text-gray-500">Loan Officers</p>
                  <p className="text-2xl font-bold">{userStats?.loanOfficers || 0}</p>
                </CardContent>
              </Card>
            </div>
            
            <Card>
              <CardHeader>
                <CardTitle>User Management</CardTitle>
              </CardHeader>
              <CardContent>
                <UserManagementTable />
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="ai" className="space-y-6">
            <div className="grid grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Model Performance</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <MetricRow label="Health Score Model" value="92%" status="healthy" />
                    <MetricRow label="Fraud Detection" value="88%" status="healthy" />
                    <MetricRow label="Forecasting" value="85%" status="warning" />
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <CardTitle>Inference Metrics</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <MetricRow label="Avg Response Time" value="120ms" status="healthy" />
                    <MetricRow label="Requests/min" value="450" status="healthy" />
                    <MetricRow label="Error Rate" value="0.1%" status="healthy" />
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
          
          <TabsContent value="system" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Service Status</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <ServiceStatus name="Auth Service" status="healthy" uptime="99.9%" />
                  <ServiceStatus name="MSME Service" status="healthy" uptime="99.8%" />
                  <ServiceStatus name="Scoring Service" status="healthy" uptime="99.9%" />
                  <ServiceStatus name="Fraud Service" status="healthy" uptime="99.7%" />
                  <ServiceStatus name="AI Scoring" status="healthy" uptime="99.8%" />
                  <ServiceStatus name="AI Forecasting" status="warning" uptime="98.5%" />
                </div>
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="audit" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Recent Audit Logs</CardTitle>
              </CardHeader>
              <CardContent>
                <AuditLogsTable />
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

const SystemHealthCard: React.FC<{
  title: string;
  value: string;
  status: 'healthy' | 'warning' | 'critical';
}> = ({ title, value, status }) => (
  <Card>
    <CardContent className="p-4">
      <div className="flex items-center gap-3">
        <div className={`w-3 h-3 rounded-full ${
          status === 'healthy' ? 'bg-green-500' :
          status === 'warning' ? 'bg-yellow-500' : 'bg-red-500'
        }`} />
        <div>
          <p className="text-sm text-gray-500">{title}</p>
          <p className="text-xl font-bold">{value}</p>
        </div>
      </div>
    </CardContent>
  </Card>
);

const MetricRow: React.FC<{
  label: string;
  value: string;
  status: string;
}> = ({ label, value, status }) => (
  <div className="flex items-center justify-between">
    <span className="text-sm">{label}</span>
    <div className="flex items-center gap-2">
      <span className="font-medium">{value}</span>
      <div className={`w-2 h-2 rounded-full ${
        status === 'healthy' ? 'bg-green-500' : 'bg-yellow-500'
      }`} />
    </div>
  </div>
);

const ServiceStatus: React.FC<{
  name: string;
  status: string;
  uptime: string;
}> = ({ name, status, uptime }) => (
  <div className="flex items-center justify-between p-2 bg-gray-50 rounded">
    <div className="flex items-center gap-2">
      <div className={`w-2 h-2 rounded-full ${
        status === 'healthy' ? 'bg-green-500' : 'bg-yellow-500'
      }`} />
      <span>{name}</span>
    </div>
    <span className="text-sm text-gray-500">Uptime: {uptime}</span>
  </div>
);

const UserManagementTable: React.FC = () => (
  <div className="text-center py-8 text-gray-500">
    User Management Table
  </div>
);

const AuditLogsTable: React.FC = () => (
  <div className="text-center py-8 text-gray-500">
    Audit Logs Table
  </div>
);

export default AdminDashboard;
```

---

## 11.6 DASHBOARD LAYOUT

```tsx
// frontend/src/components/dashboards/DashboardLayout.tsx
import React from 'react';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children }) => {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
};

// frontend/src/components/dashboards/Sidebar.tsx
import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

const menuItems = {
  msme: [
    { path: '/msme', label: 'Dashboard', icon: '📊' },
    { path: '/msme/profile', label: 'Profile', icon: '👤' },
    { path: '/msme/health-card', label: 'Health Card', icon: '💳' },
    { path: '/msme/loans', label: 'Loans', icon: '🏦' },
    { path: '/msme/reports', label: 'Reports', icon: '📋' },
  ],
  loan_officer: [
    { path: '/loan-officer', label: 'Dashboard', icon: '📊' },
    { path: '/loan-officer/applications', label: 'Applications', icon: '📝' },
    { path: '/loan-officer/msme', label: 'MSME Directory', icon: '📁' },
  ],
  credit_manager: [
    { path: '/credit-manager', label: 'Dashboard', icon: '📊' },
    { path: '/credit-manager/portfolio', label: 'Portfolio', icon: '💼' },
    { path: '/credit-manager/risk', label: 'Risk Analysis', icon: '⚠️' },
    { path: '/credit-manager/benchmarks', label: 'Benchmarks', icon: '📈' },
  ],
  admin: [
    { path: '/admin', label: 'Dashboard', icon: '📊' },
    { path: '/admin/users', label: 'Users', icon: '👥' },
    { path: '/admin/system', label: 'System', icon: '⚙️' },
    { path: '/admin/audit', label: 'Audit Logs', icon: '📋' },
  ]
};

export const Sidebar: React.FC = () => {
  const { user } = useAuth();
  const location = useLocation();
  
  const items = menuItems[user?.role || 'msme'] || menuItems.msme;
  
  return (
    <div className="w-64 bg-white border-r">
      <div className="p-4 border-b">
        <h1 className="text-xl font-bold text-blue-600">MSME Platform</h1>
        <p className="text-sm text-gray-500">IDBI Bank</p>
      </div>
      
      <nav className="p-4 space-y-2">
        {items.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
              location.pathname === item.path
                ? 'bg-blue-50 text-blue-600'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            <span>{item.icon}</span>
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>
    </div>
  );
};

// frontend/src/components/dashboards/Header.tsx
import React from 'react';
import { NotificationBell } from '../shared/NotificationBell';
import { UserMenu } from '../shared/UserMenu';

export const Header: React.FC = () => {
  return (
    <header className="h-16 bg-white border-b flex items-center justify-between px-6">
      <div className="flex items-center gap-4">
        <input
          type="search"
          placeholder="Search..."
          className="border rounded-lg px-4 py-2 w-64"
        />
      </div>
      
      <div className="flex items-center gap-4">
        <NotificationBell />
        <UserMenu />
      </div>
    </header>
  );
};
```

---

## 11.7 ROUTING CONFIGURATION

```tsx
// frontend/src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuth } from '@/hooks/useAuth';

// Dashboards
import MSMEDashboard from '@/pages/dashboards/MSMEDashboard';
import LoanOfficerDashboard from '@/pages/dashboards/LoanOfficerDashboard';
import CreditManagerDashboard from '@/pages/dashboards/CreditManagerDashboard';
import AdminDashboard from '@/pages/dashboards/AdminDashboard';

// Auth Pages
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';

const queryClient = new QueryClient();

const ProtectedRoute: React.FC<{
  children: React.ReactNode;
  roles: string[];
}> = ({ children, roles }) => {
  const { user, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) return <Navigate to="/login" />;
  if (!roles.includes(user?.role || '')) return <Navigate to="/unauthorized" />;
  
  return <>{children}</>;
};

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          {/* MSME Routes */}
          <Route path="/msme/*" element={
            <ProtectedRoute roles={['msme']}>
              <MSMEDashboard />
            </ProtectedRoute>
          } />
          
          {/* Loan Officer Routes */}
          <Route path="/loan-officer/*" element={
            <ProtectedRoute roles={['loan_officer']}>
              <LoanOfficerDashboard />
            </ProtectedRoute>
          } />
          
          {/* Credit Manager Routes */}
          <Route path="/credit-manager/*" element={
            <ProtectedRoute roles={['credit_manager']}>
              <CreditManagerDashboard />
            </ProtectedRoute>
          } />
          
          {/* Admin Routes */}
          <Route path="/admin/*" element={
            <ProtectedRoute roles={['admin']}>
              <AdminDashboard />
            </ProtectedRoute>
          } />
          
          {/* Default Redirect */}
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
```

---

## 11.8 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Dashboard Layout & Navigation | 2 days |
| MSME Dashboard | 3 days |
| Loan Officer Dashboard | 3 days |
| Credit Manager Dashboard | 3 days |
| Admin Dashboard | 2 days |
| API Integration | 2 days |
| Testing | 2 days |
| **Total** | **17 days** |

---

## 11.9 HACKATHON PRIORITY

**HIGH** - Core user interface demonstrating the platform
