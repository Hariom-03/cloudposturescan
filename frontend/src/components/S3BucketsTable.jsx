import React from 'react';

const S3BucketsTable = ({ buckets }) => {
    if (!buckets || buckets.length === 0) {
        return (
            <div className="empty-state">
                <div className="empty-state-icon">🪣</div>
                <h3>No S3 Buckets Found</h3>
                <p className="text-muted">Run a scan to discover S3 buckets in your AWS account</p>
            </div>
        );
    }

    return (
        <div className="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Bucket Name</th>
                        <th>Region</th>
                        <th>Encryption</th>
                        <th>Access Policy</th>
                        <th>Public Access Block</th>
                        <th>Versioning</th>
                    </tr>
                </thead>
                <tbody>
                    {buckets.map((bucket) => (
                        <tr key={bucket.bucketName}>
                            <td>
                                <code style={{ color: 'var(--accent-primary)' }}>
                                    {bucket.bucketName}
                                </code>
                            </td>
                            <td>
                                <span className="badge badge-info">{bucket.region}</span>
                            </td>
                            <td>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <span className={`badge ${bucket.encryptionEnabled ? 'badge-success' : 'badge-error'
                                        }`}>
                                        {bucket.encryptionEnabled ? '✓ Enabled' : '✗ Disabled'}
                                    </span>
                                    {bucket.encryptionEnabled && (
                                        <code style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                                            {bucket.encryptionType}
                                        </code>
                                    )}
                                </div>
                            </td>
                            <td>
                                <span className={`badge ${bucket.accessPolicy === 'PRIVATE' ? 'badge-success' : 'badge-error'
                                    }`}>
                                    {bucket.accessPolicy}
                                </span>
                            </td>
                            <td>
                                <span className={`badge ${bucket.blockPublicAccess ? 'badge-success' : 'badge-error'
                                    }`}>
                                    {bucket.blockPublicAccess ? '✓ Enabled' : '✗ Disabled'}
                                </span>
                            </td>
                            <td>
                                <span className={`badge ${bucket.versioningEnabled ? 'badge-success' : 'badge-warning'
                                    }`}>
                                    {bucket.versioningEnabled ? '✓ Enabled' : '✗ Disabled'}
                                </span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default S3BucketsTable;
