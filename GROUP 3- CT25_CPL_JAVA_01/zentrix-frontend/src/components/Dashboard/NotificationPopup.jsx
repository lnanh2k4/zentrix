import { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';

const NotificationPopup = ({ message, duration = 3000, onClose, type = 'default' }) => {
    const [isVisible, setIsVisible] = useState(false);

    const getBackgroundClass = () => {
        switch (type) {
            case 'complete':
                return 'bg-gradient-to-br from-green-500 to-emerald-600'; // Updated to match ConfirmPopup
            case 'fail':
                return 'bg-gradient-to-br from-red-600 to-pink-700';
            default:
                return 'bg-gradient-to-br from-indigo-600 to-purple-700';
        }
    };

    useEffect(() => {
        if (message) {
            console.log('NotificationPopup mounted with message:', message, 'type:', type);
            setIsVisible(true);

            const timer = setTimeout(() => {
                console.log('Timer triggered, hiding notification');
                setIsVisible(false);
            }, duration);

            return () => {
                console.log('Cleaning up timer');
                clearTimeout(timer);
            };
        }
    }, [message, duration, onClose, type]);

    const handleAnimationEnd = () => {
        if (!isVisible) {
            console.log('Animation ended, calling onClose');
            if (onClose) onClose();
        }
    };

    if (!message) {
        console.log('No message, returning null');
        return null;
    }

    console.log('Rendering NotificationPopup, isVisible:', isVisible);

    return (
        <div
            className={`fixed top-6 right-6 max-w-xs w-full p-4 rounded-xl shadow-2xl text-white transform ${isVisible ? 'animate-slide-in opacity-100' : 'animate-slide-out opacity-0'
                } ${getBackgroundClass()}`}
            style={{ zIndex: 9999 }}
            onAnimationEnd={handleAnimationEnd}
        >
            {/* Decorative top gradient bar */}
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-white/20 to-transparent rounded-t-xl" />

            <div className="flex items-center space-x-4">
                {/* Icon with subtle background */}
                <div className="flex-shrink-0 p-2 rounded-full bg-white/10">
                    <svg
                        className="w-6 h-6 text-white"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d={
                                type === 'complete'
                                    ? 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z' // Checkmark for success
                                    : type === 'fail'
                                        ? 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z' // X for fail
                                        : 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' // Info for default
                            }
                        />
                    </svg>
                </div>

                {/* Message */}
                <span className="text-base font-semibold flex-1 leading-tight tracking-wide">
                    {message}
                </span>

                {/* Close Button */}
                <button
                    onClick={() => {
                        console.log('Close button clicked');
                        setIsVisible(false);
                    }}
                    className="flex-shrink-0 p-1 rounded-full hover:bg-white/20 transition-colors duration-200"
                >
                    <svg
                        className="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M6 18L18 6M6 6l12 12"
                        />
                    </svg>
                </button>
            </div>
        </div>
    );
};

const showNotification = (message, duration = 3000, type = 'default') => {
    console.log('showNotification called with message:', message, 'type:', type);
    const notificationContainer = document.createElement('div');
    document.body.appendChild(notificationContainer);

    const renderNotification = () => {
        const root = createRoot(notificationContainer);
        const handleClose = () => {
            console.log('Unmounting notification');
            root.unmount();
            document.body.removeChild(notificationContainer);
        };

        root.render(
            <NotificationPopup
                message={message}
                duration={duration}
                onClose={handleClose}
                type={type}
            />
        );
    };

    renderNotification();
};

export { NotificationPopup, showNotification };