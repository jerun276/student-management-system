import React from 'react';
import ReactDOM from 'react-dom/client';
import PerformanceChart from './PerformanceChart';

// This finds the div in your Thymeleaf template and mounts the React component
const root = ReactDOM.createRoot(document.getElementById('react-performance-chart'));
root.render(
    <React.StrictMode>
        <PerformanceChart/>
    </React.StrictMode>
);