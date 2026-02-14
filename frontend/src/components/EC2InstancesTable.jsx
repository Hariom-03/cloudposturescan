import React from 'react';

const EC2InstancesTable = ({ instances }) => {
    if (!instances || instances.length === 0) {
        return (
            <div className="empty-state">
                <div className="empty-state-icon">💻</div>
                <h3>No EC2 Instances Found</h3>
                <p className="text-muted">Run a scan to discover EC2 instances in your AWS account</p>
            </div>
        );
    }

    return (
        <div className="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Instance ID</th>
                        <th>Type</th>
                        <th>Region</th>
                        <th>Public IP</th>
                        <th>State</th>
                        <th>Security Groups</th>
                    </tr>
                </thead>
                <tbody>
                    {instances.map((instance) => (
                        <tr key={instance.instanceId}>
                            <td>
                                <code style={{ color: 'var(--accent-primary)' }}>
                                    {instance.instanceId}
                                </code>
                            </td>
                            <td>{instance.instanceType}</td>
                            <td>
                                <span className="badge badge-info">{instance.region}</span>
                            </td>
                            <td>
                                {instance.publicIp !== 'N/A' ? (
                                    <code>{instance.publicIp}</code>
                                ) : (
                                    <span className="text-muted">No public IP</span>
                                )}
                            </td>
                            <td>
                                <span className={`badge ${instance.state === 'RUNNING' ? 'badge-success' :
                                        instance.state === 'STOPPED' ? 'badge-warning' :
                                            'badge-error'
                                    }`}>
                                    {instance.state}
                                </span>
                            </td>
                            <td>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                    {instance.securityGroups.map((sg, index) => (
                                        <code key={index} style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                                            {sg}
                                        </code>
                                    ))}
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default EC2InstancesTable;
