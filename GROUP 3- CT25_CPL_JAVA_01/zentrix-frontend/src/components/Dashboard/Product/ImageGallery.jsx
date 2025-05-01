import { useState, useEffect } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Navigation, Thumbs, FreeMode } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import 'swiper/css/free-mode';
import 'swiper/css/thumbs';

const ImageGallery = ({ imagePreviews, isEditMode = false, onRemove }) => {
    const [thumbsSwiper, setThumbsSwiper] = useState(null);
    const [selectedImage, setSelectedImage] = useState(null);

    useEffect(() => {
        console.log(imagePreviews)
        if (thumbsSwiper && !thumbsSwiper.destroyed) {
            thumbsSwiper.update();
        }
    }, [thumbsSwiper, imagePreviews]); // Thêm dependency

    const handleImageClick = (imageUrl) => {
        setSelectedImage(imageUrl);
    };

    const handleRemoveClick = (index) => {
        if (onRemove) {
            onRemove(index);
        }
    };

    return (
        <div className="mt-2">
            {imagePreviews && imagePreviews.length > 0 ? (
                <div>
                    {/* Main Swiper (Primary Image) */}
                    <Swiper
                        modules={[Navigation, Thumbs, FreeMode]}
                        spaceBetween={10}
                        navigation
                        thumbs={{ swiper: thumbsSwiper && !thumbsSwiper.destroyed ? thumbsSwiper : null }}
                        className="w-full h-96 mb-2 rounded-lg"
                    >
                        {imagePreviews.map((imageUrl, index) => {
                            const fullImageUrl = imageUrl.startsWith('blob:') || imageUrl.startsWith('http')
                                ? imageUrl
                                : `http://localhost:6789${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`;
                            console.log("Rendering Main Image URL:", fullImageUrl); // Debug
                            return (
                                <SwiperSlide key={index}>
                                    <div
                                        className="relative flex items-center justify-center h-full group cursor-pointer overflow-hidden rounded-lg"
                                        onClick={() => handleImageClick(imageUrl)}
                                    >
                                        <img
                                            src={fullImageUrl}
                                            alt={`Image ${index}`}
                                            className="w-auto h-full max-h-96 object-center transition-transform duration-300 ease-in-out group-hover:scale-105 group-hover:shadow-lg"
                                            onError={(e) => {
                                                console.error("Main Image load error for URL:", fullImageUrl);
                                                e.target.src = '/placeholder-image.jpg';
                                            }}
                                        />
                                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-opacity duration-300"></div>

                                    </div>
                                </SwiperSlide>
                            );
                        })}
                    </Swiper>

                    {/* Thumbnail Swiper (Thumbnails below) */}
                    <Swiper
                        onSwiper={setThumbsSwiper}
                        spaceBetween={10}
                        slidesPerView={5}
                        freeMode
                        watchSlidesProgress
                        modules={[FreeMode, Navigation, Thumbs]}
                        className="w-full"
                    >
                        {imagePreviews.map((imageUrl, index) => {
                            const fullImageUrl = imageUrl.startsWith('blob:') || imageUrl.startsWith('http')
                                ? imageUrl
                                : `http://localhost:6789${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`;
                            console.log("Rendering Thumbnail URL:", fullImageUrl); // Debug
                            return (
                                <SwiperSlide key={index}>
                                    <div className="relative cursor-pointer overflow-hidden rounded-md border border-border">
                                        <img
                                            src={fullImageUrl}
                                            alt={`Thumbnail ${index}`}
                                            className="w-full h-12 object-cover opacity-80 hover:opacity-100 transition-opacity duration-200"
                                            onError={(e) => {
                                                console.error("Thumbnail load error for URL:", fullImageUrl);
                                                e.target.src = '/placeholder-image.jpg';
                                            }}
                                        />
                                        {isEditMode && (
                                            <button
                                                type="button"
                                                onClick={() => handleRemoveClick(index)}
                                                className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center hover:bg-red-600 transition-colors duration-200"
                                            >
                                                ×
                                            </button>
                                        )}
                                    </div>
                                </SwiperSlide>
                            );
                        })}
                    </Swiper>
                </div>
            ) : (
                <p className="text-muted-foreground text-sm">No images available</p>
            )}

            {/* Expanded Image Modal */}
            {selectedImage && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 animate-in fade-in-0 duration-300"
                    onClick={() => setSelectedImage(null)}
                >
                    <div className="relative max-w-4xl w-full p-6">
                        <img
                            src={
                                selectedImage.startsWith('blob:') || selectedImage.startsWith('http')
                                    ? selectedImage
                                    : `http://localhost:6789${selectedImage.startsWith('/') ? '' : '/'}${selectedImage}`
                            }
                            alt="Expanded Image"
                            className="w-full h-auto max-h-[85vh] object-contain rounded-xl shadow-2xl"
                            onError={(e) => {
                                console.error("Expanded Image load error for URL:", selectedImage);
                                e.target.src = '/placeholder-image.jpg';
                            }}
                        />
                        <button
                            className="absolute top-4 right-4 p-2 bg-gray-900/80 text-white rounded-full hover:bg-gray-700 transition-colors duration-200"
                            onClick={() => setSelectedImage(null)}
                        >
                            ✕
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ImageGallery;