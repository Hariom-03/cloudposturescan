import React from 'react';

const DashboardSummary = ({ summary }) => {
    if (!summary) return null;

    const complianceRate = summary.complianceRate || 0;
    const getComplianceColor = (rate) => {
        if (rate >= 80) return 'var(--success)';
        if (rate >= 60) return 'var(--warning)';
        return 'var(--error)';
    };

    return (
        <div className="stats-grid">
            <div className="stat-card">
                <div className="stat-label">EC2 Instances</div>
                <div className="stat-value">{summary.totalEC2Instances || 0}</div>
                <div className="stat-icon">💻</div>
            </div>

            <div className="stat-card">
                <div className="stat-label">S3 Buckets</div>
                <div className="stat-value">{summary.totalS3Buckets || 0}</div>
                <div className="stat-icon">🪣</div>
            </div>

            <div className="stat-card">
                <div className="stat-label">Security Checks</div>
                <div className="stat-value">{summary.totalCISChecks || 0}</div>
                <div className="stat-icon">✓</div>
            </div>

            <div className="stat-card">
                <div className="stat-label">Compliance Rate</div>
                <div
                    className="stat-value"
                    style={{ color: getComplianceColor(complianceRate) }}
                >
                    {complianceRate}%
                </div>
                <div className="stat-icon">📊</div>
            </div>

            <div className="stat-card">
                <div className="stat-label">Checks Passed</div>
                <div className="stat-value text-success">{summary.checksPassedCount || 0}</div>
                <div className="stat-icon">✅</div>
            </div>

            <div className="stat-card">
                <div className="stat-label">Checks Failed</div>
                <div className="stat-value text-error">{summary.checksFailedCount || 0}</div>
                <div className="stat-icon">❌</div>
            </div>
        </div>
    );
};

export default DashboardSummary;
