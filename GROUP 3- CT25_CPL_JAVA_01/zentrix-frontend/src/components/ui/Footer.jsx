import { Facebook, Instagram, Twitter, Youtube } from "lucide-react";
import { Link } from "react-router-dom"; // Import Link from react-router-dom

const Footer = () => {
    return (
        <footer className="bg-[#e3efff] text-[#2c65aa] w-full py-20">
            <div className="container mx-auto px-8 text-center md:text-left">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8 pb-10">
                    <div>
                        <h3 className="text-3xl font-extrabold uppercase">Zentrix</h3>
                        <p className="text-md mt-3 opacity-80 leading-relaxed pb-8">
                            Zentrix is the leading retail system for genuine electronic devices, laptops, phones and accessories in Vietnam.
                        </p>
                        <div className="flex justify-center md:justify-start space-x-4 mt-3">
                            <a href="#" className="text-[#2c65aa] hover:text-blue-600 transition" aria-label="Facebook"><Facebook size={32}/></a>
                            <a href="#" className="text-[#2c65aa] hover:text-blue-600 transition" aria-label="Instagram"><Instagram size={32}/></a>
                            <a href="#" className="text-[#2c65aa] hover:text-blue-600 transition" aria-label="Twitter"><Twitter size={32}/></a>
                            <a href="#" className="text-[#2c65aa] hover:text-blue-600 transition" aria-label="Youtube"><Youtube size={32} /></a>
                        </div>
                    </div>
                    <div>
                        <h4 className="text-xl font-semibold">Product Category</h4>
                        <ul className="mt-3 space-y-3">
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Phone</a>
                            </li>
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Laptop</a>
                            </li>
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Tablet</a>
                            </li>
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Accessories</a>
                            </li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="text-xl font-semibold">Customer Service</h4>
                        <ul className="mt-3 space-y-3">
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Warranty Policy</a>
                            </li>
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Return Policy</a>
                            </li>
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Purchase Instructions</a>
                            </li>
                            <li>
                                <a href="#" className="hover:underline hover:text-[#A0BFE0] transition duration-300">Warranty Center</a>
                            </li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="text-xl font-semibold">Contact</h4>
                        <p className="text-md mt-3 opacity-80">Email: support@zentrix.com</p>
                        <p className="text-md opacity-80">Hotline: 1900 1234</p>
                        <p className="text-md opacity-80">Address: 600 Nguyen Van Cu noi dai, Ninh Kieu, Can Tho</p>
                    </div>
                </div>

                <div className="border-t border-[#2c65aa] mt-8 pt-4 text-md opacity-80 flex justify-between items-center">
                    <span>
                        © {new Date().getFullYear()} Zentrix - Electronic retail system. All rights reserved.
                    </span>
                    <div className="space-x-4">
                        <a href="#" className="hover:underline">
                            Policy & Terms of Use
                        </a>
                        <Link to="/about" className="hover:underline">
                            About Us
                        </Link>
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;