
const Footer = () => {
    return (
        <footer className="bg-[#e3efff] text-[#2c65aa] w-full px-2 py-4">
            <div className="container mx-auto px-8 text-center md:text-left">
                <div className="border-t border-[#2c65aa] pt-4 text-md opacity-80 flex justify-between items-center">
                    <span>
                        © {new Date().getFullYear()} Zentrix - Electronic retail system. All rights reserved.
                    </span>
                    <a href="#" className="hover:underline">
                        Policy & Terms of Use
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;