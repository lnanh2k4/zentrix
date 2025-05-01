import { getInfo, logout } from '@/context/ApiContext';
import { checkUserRole } from '@/services/InfoService';
import { GoogleLogin } from '@react-oauth/google';
import axios from 'axios';
import { showNotification } from "../Dashboard/NotificationPopup"; // Add this import

function GoogleLoginButton() {
    const LOCKED_STATUS = [3, 4]; // Status codes for locked accounts

    const handleSuccess = async (credentialResponse) => {
        try {
            const { credential } = credentialResponse;
            const response = await axios.post('http://localhost:6789/api/v1/auth/google', {
                idToken: credential,
            });
            const jwt = response.data.content;
            document.cookie = `jwt=${jwt};expires=${new Date(Date.now() + 86400000).toUTCString()};path=/; SameSite=Strict; Secure=false;`;

            const userData = await getInfo();
            console.log("User Data: ", userData);

            const status = userData?.content?.status;

            // Check if account is locked (status 3 or 4)
            if (LOCKED_STATUS.includes(status)) {
                showNotification("Your account has been locked. Please contact support.", 3000, 'fail');
                // Perform logout action here if needed
                await logout()
                return;
            }

            const role = checkUserRole(userData?.content);
            console.log("Is not Customer: ", !role.isCustomer());
            console.log("Check Condition: ", userData && !role.isCustomer());

            if (userData && !role.isCustomer()) {
                console.log("Condition true");
                window.location.href = "/dashboard";
            } else {
                console.log("Condition false");
                window.location.href = "/";
            }
        } catch (error) {
            console.error('Login failed:', error);
        }
    };

    const handleError = () => {
        console.log('Login Failed');
    };

    return (
        <div className="w-full md:w-auto">
            <GoogleLogin
                onSuccess={handleSuccess}
                onError={handleError}
                use_fedcm_for_prompt={false}
                useOneTap
                type="standard"
                className="w-full px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition duration-200"
            />
        </div>
    );
}

export default GoogleLoginButton;