import React, {useState, useEffect} from 'react';

const Announcement = () => {
    const [announcement, setAnnouncement] = useState(null);
    const [isVisible, setIsVisible] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fetch the announcement data when the component mounts
        fetch('/api/public/latest-announcement')
            .then(response => {
                if (response.status === 204) { // 204 No Content
                    return null;
                }
                if (!response.ok) {
                    throw new Error('Failed to fetch announcement');
                }
                return response.json();
            })
            .then(data => {
                if (data) {
                    setAnnouncement(data);
                    setIsVisible(true); // Only show if there is an announcement
                }
            })
            .catch(error => {
                setError(error.message);
                // We won't show an error to the user, but we'll log it for debugging
                console.error("Error fetching announcement:", error);
            });
    }, []); // Empty array ensures this runs only once

    // This is the handler for the close button
    const handleClose = () => {
        setIsVisible(false);
    };

    // If there's no announcement or it has been closed, render nothing
    if (!announcement || !isVisible) {
        return null;
    }

    // Otherwise, render the announcement banner
    return (
        <div className="bg-primary text-primary-content">
            <div className="container mx-auto p-4 flex justify-between items-center">
                <div className="text-center flex-grow">
                    <h3 className="font-bold text-lg">{announcement.title}</h3>
                    <p>{announcement.content}</p>
                </div>
                <button onClick={handleClose} className="btn btn-sm btn-ghost">
                    ✕
                </button>
            </div>
        </div>
    );
};

export default Announcement;