import React from 'react';

const Header = ({ onScan, isScanning }) => {
    return (
        <header className="header">
            <div className="header-content">
                <div className="logo">
                    <div className="logo-icon">🛡️</div>
                    <div className="logo-text">Cloud Posture Scanner</div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={onScan}
                    disabled={isScanning}
                >
                    {isScanning ? (
                        <>
                            <span className="spinner"></span>
                            Scanning...
                        </>
                    ) : (
                        <>
                            <span>🔍</span>
                            Run Security Scan
                        </>
                    )}
                </button>
            </div>
        </header>
    );
};

export default Header;
