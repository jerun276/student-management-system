import React from 'react';
import ReactDOM from 'react-dom/client';
import PerformanceChart from './PerformanceChart';
import Announcement from './Announcement'; // <-- IMPORT THE NEW COMPONENT

// --- Mount the Performance Chart (Existing) ---
const chartContainer = document.getElementById('react-performance-chart');
if (chartContainer) {
    const chartRoot = ReactDOM.createRoot(chartContainer);
    chartRoot.render(
        <React.StrictMode>
            <PerformanceChart/>
        </React.StrictMode>
    );
}

// --- Mount the Announcement Component (NEW) ---
const announcementContainer = document.getElementById('react-announcement');
if (announcementContainer) {
    const announcementRoot = ReactDOM.createRoot(announcementContainer);
    announcementRoot.render(
        <React.StrictMode>
            <Announcement/>
        </React.StrictMode>
    );
}