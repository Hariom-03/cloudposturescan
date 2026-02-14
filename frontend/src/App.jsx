import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import DashboardSummary from './components/DashboardSummary';
import EC2InstancesTable from './components/EC2InstancesTable';
import S3BucketsTable from './components/S3BucketsTable';
import CISResultsTable from './components/CISResultsTable';
import {
    triggerScan,
    getEC2Instances,
    getS3Buckets,
    getCISResults,
    getDashboardSummary,
} from './services/api';
import './index.css';

function App() {
    const [loading, setLoading] = useState(true);
    const [scanning, setScanning] = useState(false);
    const [summary, setSummary] = useState(null);
    const [ec2Instances, setEc2Instances] = useState([]);
    const [s3Buckets, setS3Buckets] = useState([]);
    const [cisResults, setCisResults] = useState([]);
    const [error, setError] = useState(null);

    const loadDashboardData = async () => {
        try {
            setLoading(true);
            setError(null);

            const [summaryData, instancesData, bucketsData, resultsData] = await Promise.all([
                getDashboardSummary(),
                getEC2Instances(),
                getS3Buckets(),
                getCISResults(),
            ]);

            setSummary(summaryData);
            setEc2Instances(instancesData);
            setS3Buckets(bucketsData);
            setCisResults(resultsData);
        } catch (err) {
            console.error('Error loading dashboard data:', err);
            setError('Failed to load dashboard data. Please ensure the backend is running.');
        } finally {
            setLoading(false);
        }
    };

    const handleScan = async () => {
        try {
            setScanning(true);
            setError(null);

            const scanResponse = await triggerScan();
            console.log('Scan completed:', scanResponse);

            // Show success message
            alert(`Scan completed successfully!\n\nEC2 Instances: ${scanResponse.ec2InstancesFound}\nS3 Buckets: ${scanResponse.s3BucketsFound}\nChecks Passed: ${scanResponse.checksPassed}/${scanResponse.checksPerformed}`);

            // Reload dashboard data
            await loadDashboardData();
        } catch (err) {
            console.error('Error running scan:', err);
            setError('Failed to run scan. Please check your AWS credentials and permissions.');
            alert('Scan failed! Please check the console for details.');
        } finally {
            setScanning(false);
        }
    };

    useEffect(() => {
        loadDashboardData();
    }, []);

    if (loading) {
        return (
            <>
                <Header onScan={handleScan} isScanning={scanning} />
                <div className="loading-container">
                    <div className="loading-spinner"></div>
                    <h3>Loading Dashboard...</h3>
                    <p className="text-muted">Fetching data from AWS</p>
                </div>
            </>
        );
    }

    return (
        <>
            <Header onScan={handleScan} isScanning={scanning} />

            <div className="container">
                {error && (
                    <div className="card" style={{
                        background: 'var(--error-bg)',
                        borderColor: 'var(--error)',
                        marginBottom: '2rem'
                    }}>
                        <h3 style={{ color: 'var(--error)' }}>⚠️ Error</h3>
                        <p>{error}</p>
                    </div>
                )}

                <section className="section">
                    <h2>Dashboard Overview</h2>
                    <DashboardSummary summary={summary} />
                </section>

                <section className="section">
                    <div className="section-header">
                        <h2>CIS AWS Benchmark Results</h2>
                    </div>
                    <div className="card">
                        <CISResultsTable results={cisResults} />
                    </div>
                </section>

                <section className="section">
                    <div className="section-header">
                        <h2>EC2 Instances</h2>
                    </div>
                    <div className="card">
                        <EC2InstancesTable instances={ec2Instances} />
                    </div>
                </section>

                <section className="section">
                    <div className="section-header">
                        <h2>S3 Buckets</h2>
                    </div>
                    <div className="card">
                        <S3BucketsTable buckets={s3Buckets} />
                    </div>
                </section>

                <footer style={{
                    textAlign: 'center',
                    padding: '2rem',
                    color: 'var(--text-muted)',
                    borderTop: '1px solid var(--border-color)',
                    marginTop: '3rem'
                }}>
                    <p>Cloud Posture Scanner v1.0 | Built with Java Spring Boot & React</p>
                    <p style={{ fontSize: '0.875rem', marginTop: '0.5rem' }}>
                        Performs CIS AWS Benchmark security assessments
                    </p>
                </footer>
            </div>
        </>
    );
}

export default App;
