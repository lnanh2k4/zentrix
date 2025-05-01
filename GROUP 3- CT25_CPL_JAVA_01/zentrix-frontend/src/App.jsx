import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import './App.css';
// import '@fontsource/roboto/300.css';
// import '@fontsource/roboto/400.css';
// import '@fontsource/roboto/500.css';
// import '@fontsource/roboto/700.css';
import Dashboard from './components/Dashboard/Dashboard';
import Homepage from './components/Homepage/Homepage';
import ProductPage from './components/Homepage/Product/ProductPage';
import ProductDetail from './components/Homepage/ProductDetail/ProductDetail';
import Error from './components/Error/Error'
import About from './components/AboutUs/AboutUs';
import BlogPage from './components/Blog/BlogPage';
import CartPage from './components/Homepage/Cart/CartPage';
import OrderPage from './components/Homepage/Cart/OrderPage';
import Home from './components/Homepage/Customer/Home';
import MembershipPage from './components/Homepage/Customer/MembershipPage';
import OrderHistoryPage from './components/Homepage/Customer/OrderHistoryPage';
import MyPromotionPage from './components/Homepage/Customer/MyPromotionPage';
import PaymentSuccessPage from './components/Homepage/Cart/PaymentSuccessPage';
import OrderSuccessPage from './components/Homepage/Cart/OrderSuccessPage';
import Register from './components/Homepage/Auth/Register';
import Login from './components/Homepage/Auth/Login';
import Logout from './components/Homepage/Auth/Logout';
import CompareProducts from './components/Compare/CompareProducts';
import MyWarrantyPage from './components/Homepage/Customer/MyWarrantyPage';
import BlogDetailPage from './components/Blog/BlogDetailPage';


import OrderFailurePage from './components/Homepage/Cart/OrderFailurePage';

import MyProfile from './components/Homepage/Customer/MyProfilePage';

import ForgotPasswordEmail from './components/Homepage/Auth/ForgotPasswordEmail';
import ResetPassword from './components/Homepage/Auth/ResetPassword';
import EmailSentForForgotPasswordPage from './components/Homepage/Auth/EmailSentPage';
import EmailVerifiedPage from './components/Homepage/Auth/EmailVerifiedPage';
function App() {
  const [count, setCount] = useState(0);

  return (
    <Router>
      <Routes>
        {/* Homepage */}
        <Route path="/" element={<Homepage />} />
        <Route path="/product/:productId" element={<ProductDetail />} />
        <Route path="/products" element={<ProductPage />} />
        <Route path="/products/:category" element={<ProductPage />} />
        <Route path="/products/:category/:category" element={<ProductPage />} />
        <Route path="/products/:category/:category/:brand" element={<ProductPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/orderPage" element={<OrderPage />} />
        <Route path="/order-processing" element={<OrderFailurePage />} />
        <Route path="/home" element={<Home />} />
        <Route path="/membershipPage" element={<MembershipPage />} />
        <Route path="/history" element={<OrderHistoryPage />} />
        <Route path="/myPromotionPage" element={<MyPromotionPage />} />
        <Route path="/MyWarrantyPage" element={<MyWarrantyPage />} />
        <Route path="/PaymentSuccessPage" element={<PaymentSuccessPage />} />
        <Route path="/order-success" element={<OrderSuccessPage />} />
        {/* <Route path="/" element={<Navigate to="/dashboard/users" />} /> */}
        <Route path="/dashboard/*" element={<Dashboard />} />
        <Route path="/about" element={<About />} />
        <Route path="/*" element={<Error />} />
        <Route path='/blog' element={<BlogPage />} />
        <Route path="/blog/:postId" element={<BlogDetailPage />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password/email" element={<ForgotPasswordEmail />} />
        <Route path="/forgot-password/reset-password" element={<ResetPassword />} />
        <Route path="/forgot-password/email-sent" element={<EmailSentForForgotPasswordPage />} />
        <Route path="/email/verified" element={<EmailVerifiedPage />} />
        <Route path="/logout" element={<Logout />} />
        <Route path="/profile" element={<MyProfile />} />
        <Route path="/compare-products" element={<CompareProducts />} />
      </Routes>
    </Router>
  );
}

export default App;
