import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import logoFull from "./logoFull.png";

export default function Error() {
  const [position, setPosition] = useState({ x: 0, y: 0 });

  useEffect(() => {
    const handleMouseMove = (event) => {
      const { clientX, clientY } = event;
      const centerX = window.innerWidth / 2;
      const centerY = window.innerHeight / 2;
      
      const moveX = (clientX - centerX) * 0.1; 
      const moveY = (clientY - centerY) * 0.1;
      
      setPosition({ x: moveX, y: moveY });
    };

    window.addEventListener("mousemove", handleMouseMove);

    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
    };
  }, []);

  return (
    <div
      className="error-container"
      style={{
        width: "100%",
        height: "100dvh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(135deg, #0044cc, #001f4d)",
        color: "white",
        textAlign: "center",
        overflow: "hidden",
        padding: "2rem",
      }}
    >
      <img
        src={logoFull}
        alt="Company Logo"
        style={{
          width: "clamp(100px, 20vw, 200px)",
          height: "auto",
          marginBottom: "1.5rem",
          filter: "drop-shadow(0 0 10px rgba(255, 255, 255, 0.5))",
        }}
      />

      <div
        style={{
          position: "relative",
          width: "100%",
          height: "50vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <div
          style={{
            position: "absolute",
            transform: `translate(${position.x}px, ${position.y}px)`,
            transition: "transform 0.1s ease-out", 
            fontSize: "clamp(15rem, 20vw, 40rem)",
            fontWeight: "bold",
            color: "#00BFFF",
            textShadow: "0 0 50px rgba(0, 191, 255, 1)",
          }}
        >
          404
        </div>
      </div>

      <p
        style={{
          fontSize: "clamp(1rem, 2vw, 1.5rem)",
          marginBottom: "2rem",
          maxWidth: "90%",
          lineHeight: "1.6",
        }}
      >
        Oops! The page you are looking for does not exist.
      </p>

      <div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.9 }}>
        <Button
          asChild
          className="bg-[#00BFFF] text-white text-[1.2rem] font-bold py-4 px-6 shadow-[0_4px_15px_rgba(0,191,255,0.5)] transition-all duration-300 hover:bg-[#008CBA] hover:shadow-[0_4px_20px_rgba(0,191,255,0.8)]"
        >
          <Link to="/">Go Home</Link>
        </Button>
      </div>
    </div>
  );
}