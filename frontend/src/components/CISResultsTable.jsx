import React from 'react';

const CISResultsTable = ({ results }) => {
    if (!results || results.length === 0) {
        return (
            <div className="empty-state">
                <div className="empty-state-icon">📋</div>
                <h3>No CIS Check Results</h3>
                <p className="text-muted">Run a scan to see CIS AWS Benchmark compliance results</p>
            </div>
        );
    }

    const getSeverityIcon = (severity) => {
        switch (severity) {
            case 'HIGH': return '🔴';
            case 'MEDIUM': return '🟡';
            case 'LOW': return '🟢';
            default: return '⚪';
        }
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'PASS': return '✅';
            case 'FAIL': return '❌';
            case 'WARNING': return '⚠️';
            default: return '❓';
        }
    };

    return (
        <div className="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Check ID</th>
                        <th>Check Name</th>
                        <th>Status</th>
                        <th>Severity</th>
                        <th>Evidence</th>
                        <th>Recommendation</th>
                    </tr>
                </thead>
                <tbody>
                    {results.map((result, index) => (
                        <tr key={`${result.checkId}-${index}`}>
                            <td>
                                <code style={{ color: 'var(--accent-primary)', fontSize: '0.875rem' }}>
                                    {result.checkId}
                                </code>
                            </td>
                            <td>
                                <strong>{result.checkName}</strong>
                                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                                    {result.description}
                                </div>
                            </td>
                            <td>
                                <span className={`badge ${result.status === 'PASS' ? 'badge-success' :
                                        result.status === 'FAIL' ? 'badge-error' :
                                            'badge-warning'
                                    }`}>
                                    {getStatusIcon(result.status)} {result.status}
                                </span>
                            </td>
                            <td>
                                <span className={`badge ${result.severity === 'HIGH' ? 'badge-error' :
                                        result.severity === 'MEDIUM' ? 'badge-warning' :
                                            'badge-info'
                                    }`}>
                                    {getSeverityIcon(result.severity)} {result.severity}
                                </span>
                            </td>
                            <td>
                                <div style={{
                                    maxWidth: '300px',
                                    fontSize: '0.875rem',
                                    color: result.status === 'PASS' ? 'var(--success)' : 'var(--text-secondary)'
                                }}>
                                    {result.evidence}
                                </div>
                            </td>
                            <td>
                                <div style={{
                                    maxWidth: '300px',
                                    fontSize: '0.875rem',
                                    color: result.status === 'FAIL' ? 'var(--warning)' : 'var(--text-muted)',
                                    fontStyle: result.recommendation === 'N/A' ? 'italic' : 'normal'
                                }}>
                                    {result.recommendation}
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default CISResultsTable;
