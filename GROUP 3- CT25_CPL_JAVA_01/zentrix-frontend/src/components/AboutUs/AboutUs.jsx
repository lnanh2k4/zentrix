import { Card, CardContent } from "@/components/ui/card";
import { useEffect, useState, useRef } from "react";
import Footer from "../ui/Footer";
import Header from "../ui/Header";
import img1 from "./Img/7.jpg";
import img2 from "./Img/1.jpg";
import img3 from "./Img/2.jpg";
import img4 from "./Img/3.jpg";
import img5 from "./Img/4.jpg";
import img6 from "./Img/5.jpg";
import img7 from "./Img/6.jpg";

const creators = [
  {
    name: "Lê Nhựt Anh",
    role: "Team Leader, Developer",
    image: img1,
    details: `
      As the Team Leader and Developer, Lê Nhựt Anh drives the project's technical vision and development. Responsibilities include:
      - Leading the development of user authentication features (Register, Login, Logout, Forgot Password).
      - Managing customer and staff functionalities (Add, Edit, Delete, Lock/Unlock, Search).
      - Overseeing profile management (View, Edit, Change Password).
      - Ensuring code quality through reviews and mentoring team members.
      - Coordinating with the team to integrate frontend and backend systems seamlessly.
    `,
  },
  {
    name: "Nguyễn Lê Khắc Vũ",
    role: "Developer",
    image: img2,
    details: `
      Nguyễn Lê Khắc Vũ contributes to critical e-commerce features. Responsibilities include:
      - Developing membership management (Add, Edit, Delete, Search, View).
      - Implementing order management (Add, View, Search, Export to PDF).
      - Integrating payment gateways (VnPay, Momo).
      - Building cart functionalities (Add, View, Edit Quantity, Delete).
      - Ensuring a smooth user experience for checkout processes.
    `,
  },
  {
    name: "Đặng Công Khanh",
    role: "Developer",
    image: img3,
    details: `
      Đặng Công Khanh focuses on product and promotion features. Responsibilities include:
      - Developing product listing and details on the homepage (View, Search, Sort, Filter).
      - Managing promotions (Add, Edit, Delete, Search, Filter, Collect).
      - Implementing warranty features (Add, View, Edit, Delete, Search, Export).
      - Ensuring efficient data retrieval for product-related APIs.
    `,
  },
  {
    name: "Huỳnh Hoàng Tỹ",
    role: "Developer",
    image: img4,
    details: `
      Huỳnh Hoàng Tỹ handles inventory and product management in the dashboard. Responsibilities include:
      - Developing inventory features (Import/Export Stock, View Details, Excel/PDF Export).
      - Managing product attributes and variations (Create, Edit, Delete, View).
      - Implementing product management (Add, Edit, Delete, Search, View).
      - Creating income statistics for business insights.
    `,
  },
  {
    name: "Võ Lâm Thúy Vi",
    role: "Developer",
    image: img5,
    details: `
      Võ Lâm Thúy Vi enhances user engagement features. Responsibilities include:
      - Developing notifications (Add, View, Delete, Search).
      - Managing reviews (Add, View, Edit, Delete, Search).
      - Implementing posts (Add, View, Edit, Delete, Search).
      - Building the price comparison feature for users.
      - Ensuring content-related features are user-friendly.
    `,
  },
  {
    name: "Nguyễn Thanh Bình",
    role: "Developer",
    image: img6,
    details: `
      Nguyễn Thanh Bình manages categories, suppliers, and branches. Responsibilities include:
      - Developing category management (Add, View, Edit, Delete, Search).
      - Implementing supplier management (Add, View, Edit, Delete, Search).
      - Building branch management (Add, View, Edit, Delete, Search, View Products).
      - Ensuring robust data handling for inventory-related features.
    `,
  },
  {
    name: "Nguyễn Việt Nguyên",
    role: "Tester",
    image: img7,
    details: `
      Nguyễn Việt Nguyên ensures the website’s quality and reliability. Responsibilities include:
      - Testing all features, including user management, products, and inventory.
      - Conducting usability tests to ensure intuitive navigation.
      - Identifying and reporting bugs across frontend and backend.
      - Performing regression testing after fixes.
      - Verifying compatibility across devices and browsers.
    `,
  },
];

export default function AboutUsPage() {
  const [isVisible, setIsVisible] = useState(false);
  const [selectedCreator, setSelectedCreator] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const timerRef = useRef(null);

  useEffect(() => {
    setIsVisible(true);
    // Auto slide đã bị tắt
  }, []);

  const handleImageClick = (creator, index) => {
    const relativeIndex = (index - currentIndex + creators.length) % creators.length;
    const offset = relativeIndex <= Math.floor(creators.length / 2)
      ? relativeIndex
      : relativeIndex - creators.length;
    const absOffset = Math.abs(offset);

    if (absOffset === 0) {
      // Nếu là card giữa, hiển thị modal ngay
      setSelectedCreator(creator);
    } else {
      // Nếu không phải card giữa, slide đến index của card này
      setCurrentIndex(index);
      // Chờ animation hoàn tất (0.5s) rồi hiển thị modal
      setTimeout(() => {
        setSelectedCreator(creator);
      }, 500); // Thời gian chờ khớp với transition (0.5s)
    }
  };

  const handleCloseModal = () => {
    setSelectedCreator(null);
  };

  const handlePrev = () => {
    setCurrentIndex((prev) => (prev - 1 + creators.length) % creators.length);
  };

  const handleNext = () => {
    setCurrentIndex((prev) => (prev + 1) % creators.length);
  };

  const getSlideStyle = (index) => {
    const relativeIndex = (index - currentIndex + creators.length) % creators.length;
    const offset = relativeIndex <= Math.floor(creators.length / 2)
      ? relativeIndex
      : relativeIndex - creators.length;
    const absOffset = Math.abs(offset);
    let scale, opacity;

    if (absOffset === 0) {
      scale = 1;
      opacity = 1;
    } else if (absOffset === 1) {
      scale = 0.85;
      opacity = 0.7;
    } else if (absOffset === 2) {
      scale = 0.7;
      opacity = 0.3;
    } else {
      scale = 0.7;
      opacity = 0;
    }

    const zIndex = 10 - absOffset;
    const translateX = offset * 300;

    return {
      transform: `translateX(${translateX}px) scale(${scale})`,
      opacity,
      zIndex,
      transition: "transform 0.5s ease, opacity 0.5s ease",
      position: "absolute",
      left: "50%",
      marginLeft: "-9rem",
    };
  };

  const fallingElements = Array.from({ length: 10 }, (_, i) => (
    <img
      key={i}
      src="/logo_zentrix.png"
      alt="Zentrix Logo"
      className="falling-element"
      style={{
        left: `${Math.random() * 100}vw`,
        animationDelay: `${Math.random() * 5}s`,
        width: "24px",
        height: "24px",
      }}
      onError={(e) => {
        e.target.src = "https://via.placeholder.com/24";
      }}
    />
  ));

  return (
    <>
      <header className="h-20 bg-blue-700 text-white flex items-center px-4 shadow-md">
        <Header />
      </header>
      <style>
        {`
          @keyframes fall {
            0% {
              transform: translateY(-100vh) rotate(0deg);
              opacity: 1;
            }
            100% {
              transform: translateY(100vh) rotate(360deg);
              opacity: 0.5;
            }
          }
          .falling-element {
            position: absolute;
            animation: fall 8s linear infinite;
            pointer-events: none;
            z-index: 10;
          }
          .carousel-container {
            position: relative;
            width: 100%;
            height: 24rem;
            overflow: hidden;
            display: flex;
            justify-content: center;
            align-items: center;
          }
          .carousel-card {
            width: 18rem !important;
            height: 22rem !important;
          }
          .carousel-card img {
            width: 8rem !important;
            height: 8rem !important;
          }
          .carousel-arrow {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 40px;
            height: 40px;
            background: rgba(0, 191, 255, 0.3);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            z-index: 20;
            color: #fff;
          }
          .carousel-arrow:hover {
            background: rgba(0, 191, 255, 0.6);
          }
          .carousel-arrow.left {
            left: 10px;
          }
          .carousel-arrow.right {
            right: 10px;
          }
          @media (max-width: 1024px) {
            .carousel-container {
              height: 22rem;
            }
            .carousel-card {
              width: 16rem !important;
              height: 20rem !important;
            }
            .carousel-card img {
              width: 7rem !important;
              height: 7rem !important;
            }
          }
          @media (max-width: 768px) {
            .carousel-container {
              height: 20rem;
            }
            .carousel-card {
              width: 14rem !important;
              height: 18rem !important;
            }
            .carousel-card img {
              width: 6rem !important;
              height: 6rem !important;
            }
          }
        `}
      </style>
      <div>
        <div
          className="min-h-screen flex flex-col items-center justify-center p-[5vh_5vw] overflow-hidden relative"
          style={{
            background: "linear-gradient(45deg, #001f4d, #003366, #0044cc, #001f4d)",
            backgroundSize: "400% 400%",
            animation: "gradientAnimation 15s ease infinite",
          }}
        >
          {fallingElements}

          <div className="absolute inset-0 bg-black/20 pointer-events-none" />

          <h1
            className={`text-6xl font-extrabold mb-12 text-white text-center transform-gpu transition-all duration-1000 tracking-wide drop-shadow-lg ${isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
              }`}
            style={{
              textShadow: "0 0 20px rgba(0, 191, 255, 0.8), 0 0 40px rgba(0, 191, 255, 0.4)",
              fontFamily: "'Montserrat', sans-serif",
              zIndex: 1,
            }}
          >
            About Us
          </h1>

          <p
            className={`mb-16 text-center text-white max-w-[60ch] text-xl leading-relaxed transform-gpu transition-all duration-1000 delay-200 font-light ${isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-10"
              }`}
            style={{ fontFamily: "'Roboto', sans-serif", zIndex: 1 }}
          >
            Welcome to Zentrix, your trusted destination for high-quality smartphones at competitive prices.
            We provide a seamless shopping experience with genuine products, nationwide delivery, and dedicated customer support.
          </p>

          <div className="w-full max-w-[90vw] transform-gpu" style={{ zIndex: 1 }}>
            <div className="carousel-container">
              {creators.map((creator, index) => (
                <Card
                  key={index}
                  className="carousel-card rounded-2xl bg-[#021526]/30 backdrop-blur-lg shadow-2xl p-6 text-center border border-[#00BFFF]/20 hover:border-[#00BFFF]/50 hover:shadow-[0_0_25px_rgba(0,191,255,0.7)]"
                  style={getSlideStyle(index)}
                  onClick={() => handleImageClick(creator, index)}
                >
                  <CardContent className="flex flex-col items-center space-y-4">
                    <img
                      src={creator.image}
                      alt={creator.name}
                      className="object-cover rounded-full mb-4 border-4 border-[#00BFFF]/40 shadow-lg hover:shadow-[0_0_15px_rgba(0,191,255,0.7)] transition-shadow duration-500 cursor-pointer"
                    />
                    <h3
                      className="text-white font-bold text-xl tracking-wide"
                      style={{
                        fontFamily: "'Montserrat', sans-serif",
                        textShadow: "0 0 10px rgba(0, 191, 255, 0.5)",
                      }}
                    >
                      {creator.name}
                    </h3>
                    <p
                      className="text-[#A0BFE0] text-base font-medium"
                      style={{ fontFamily: "'Roboto', sans-serif" }}
                    >
                      {creator.role}
                    </p>
                  </CardContent>
                </Card>
              ))}
              <div className="carousel-arrow left" onClick={handlePrev}>
                ←
              </div>
              <div className="carousel-arrow right" onClick={handleNext}>
                →
              </div>
            </div>
          </div>

          {selectedCreator && (
            <div
              className="fixed inset-0 flex items-center justify-center bg-black/50 z-50"
              role="dialog"
              aria-labelledby="modal-title"
              aria-modal="true"
            >
              <div
                className="bg-[#021526]/80 backdrop-blur-lg rounded-2xl shadow-2xl p-8 border border-[#00BFFF]/30 transform-gpu transition-all duration-500 w-full max-w-[50%] max-h-[80vh] overflow-y-auto"
                style={{
                  background: "rgba(255, 255, 255, 0.05)",
                  boxShadow: "0 0 20px rgba(0, 191, 255, 0.3)",
                }}
              >
                <div className="flex justify-between items-center mb-6">
                  <h2
                    id="modal-title"
                    className="text-3xl font-bold text-white"
                    style={{
                      fontFamily: "'Montserrat', sans-serif",
                      textShadow: "0 0 10px rgba(0, 191, 255, 0.5)",
                    }}
                  >
                    {selectedCreator.name}
                  </h2>
                  <button
                    onClick={handleCloseModal}
                    className="text-white hover:text-[#00BFFF] text-3xl leading-none"
                    aria-label="Close modal"
                  >
                    ×
                  </button>
                </div>
                <div className="space-y-4">
                  <p
                    className="text-[#A0BFE0] text-lg font-medium"
                    style={{ fontFamily: "'Roboto', sans-serif" }}
                  >
                    <span className="font-bold text-white">Role:</span> {selectedCreator.role}
                  </p>
                  <p
                    className="text-white text-base leading-relaxed whitespace-pre-line"
                    style={{ fontFamily: "'Roboto', sans-serif" }}
                  >
                    <span className="font-bold">Responsibilities:</span>
                    <br />
                    {selectedCreator.details}
                  </p>
                </div>
                <div className="flex justify-end mt-6">
                  <button
                    onClick={handleCloseModal}
                    className="bg-[#00BFFF] text-white px-6 py-2 rounded-lg hover:bg-[#00BFFF]/80 transition-all duration-300"
                    style={{ fontFamily: "'Roboto', sans-serif" }}
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
        <div>
          <Footer className="w-screen mt-auto" style={{ width: "100%" }} />
        </div>
      </div>
    </>
  );
}