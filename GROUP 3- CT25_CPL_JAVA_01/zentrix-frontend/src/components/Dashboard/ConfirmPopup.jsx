import { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';

const ConfirmPopup = ({ message, onConfirm, onCancel, type = 'default' }) => {
    const [isVisible, setIsVisible] = useState(false);

    const getBackgroundClass = () => {
        switch (type) {
            case 'create':
                return 'bg-gradient-to-br from-green-500 to-emerald-600';
            case 'fail':
                return 'bg-gradient-to-br from-red-600 to-pink-700';
            case 'edit':
                return 'bg-gradient-to-br from-blue-500 to-cyan-600';
            default:
                return 'bg-gradient-to-br from-indigo-600 to-purple-700';
        }
    };

    useEffect(() => {
        if (message) {
            setIsVisible(true);
        }
    }, [message]);

    const handleAnimationEnd = () => {
        if (!isVisible) {
            if (onCancel) onCancel();
        }
    };

    const handleYes = () => {
        setIsVisible(false);
        if (onConfirm) onConfirm();
    };

    const handleNo = () => {
        setIsVisible(false);
        if (onCancel) onCancel();
    };

    if (!message) {
        return null;
    }

    return (
        <div
            className="fixed inset-0 flex items-center justify-center z-50"
            style={{ backgroundColor: 'rgba(0, 0, 0, 0.6)' }} // Darker overlay for better contrast
        >
            <div
                className={`relative w-full max-w-sm p-8 rounded-2xl shadow-2xl text-white transform ${isVisible ? 'animate-slide-in opacity-100' : 'animate-slide-out opacity-0'
                    } ${getBackgroundClass()}`}
                onAnimationEnd={handleAnimationEnd}
            >
                {/* Decorative top gradient bar */}
                <div className="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-white/20 to-transparent rounded-t-2xl" />

                <div className="flex flex-col items-center space-y-6">
                    {/* Icon with a subtle background */}
                    <div className="p-3 rounded-full bg-white/10">
                        <svg
                            className="w-10 h-10 text-white"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                            xmlns="http://www.w3.org/2000/svg"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                            />
                        </svg>
                    </div>

                    {/* Message with improved typography */}
                    <span className="text-xl font-bold text-center leading-tight tracking-wide">
                        {message}
                    </span>

                    {/* Buttons with modern styling */}
                    <div className="flex space-x-6 mt-6">
                        <button
                            onClick={handleYes}
                            className="px-6 py-2.5 bg-white text-gray-900 font-semibold rounded-lg shadow-md hover:bg-gray-100 hover:shadow-lg transition-all duration-300 ease-in-out transform hover:-translate-y-0.5"
                        >
                            Yes
                        </button>
                        <button
                            onClick={handleNo}
                            className="px-6 py-2.5 bg-transparent border-2 border-white text-white font-semibold rounded-lg hover:bg-white/10 hover:border-white/80 transition-all duration-300 ease-in-out transform hover:-translate-y-0.5"
                        >
                            No
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

const showConfirm = (message, type = 'default') => {
    return new Promise((resolve) => {
        const confirmContainer = document.createElement('div');
        document.body.appendChild(confirmContainer);

        const root = createRoot(confirmContainer);

        const handleClose = () => {
            root.unmount();
            document.body.removeChild(confirmContainer);
        };

        const onConfirm = () => {
            resolve(true);
            handleClose();
        };

        const onCancel = () => {
            resolve(false);
            handleClose();
        };

        root.render(
            <ConfirmPopup
                message={message}
                onConfirm={onConfirm}
                onCancel={onCancel}
                type={type}
            />
        );
    });
};

export { ConfirmPopup, showConfirm };