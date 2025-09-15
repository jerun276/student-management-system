import React, { useState, useEffect } from 'react';
import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

// This registers the necessary components for a bar chart
ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

const PerformanceChart = () => {
    const [reportData, setReportData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('/api/principal/performance-report')
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(data => {
                setReportData(data);
                setLoading(false);
            })
            .catch(error => {
                setError(error.message);
                setLoading(false);
            });
    }, []);

    if (loading) return <div>Loading Chart...</div>;
    if (error) return <div>Error loading chart: {error}</div>;
    if (!reportData || reportData.length === 0) {
        return <div className="alert alert-info">No performance data available.</div>;
    }

    // Prepare data for Chart.js
    const chartLabels = reportData.map(item => item.subjectName);
    const chartValues = reportData.map(item => item.averageGrade);

    const data = {
        labels: chartLabels,
        datasets: [{
            label: 'Average Grade (%)',
            data: chartValues,
            backgroundColor: 'rgba(59, 130, 246, 0.5)',
            borderColor: 'rgba(59, 130, 246, 1)',
            borderWidth: 1,
        }],
    };

    const options = {
        responsive: true,
        plugins: {
            legend: { position: 'top' },
            title: { display: true, text: 'Average Subject Performance' },
        },
        scales: {
            y: { beginAtZero: true, max: 100 },
        },
    };

    return <Bar options={options} data={data} />;
};

export default PerformanceChart