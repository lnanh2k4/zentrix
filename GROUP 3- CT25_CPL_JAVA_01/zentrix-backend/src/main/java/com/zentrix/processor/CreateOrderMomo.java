package com.zentrix.processor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.mservice.shared.exception.MoMoException;
import com.zentrix.configuration.CustomEnvironment;
import com.zentrix.constants.Parameter;
import com.zentrix.model.request.PaymentRequest;
import com.zentrix.model.response.HttpResponse;
import com.zentrix.model.response.PaymentResponse;
import com.zentrix.model.utils.Encoder;
import com.zentrix.model.utils.Language;
import com.zentrix.model.utils.LogUtils;
import com.zentrix.model.utils.RequestType;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Service

public class CreateOrderMomo extends AbstractProcess<PaymentRequest, PaymentResponse> {

    public CreateOrderMomo(CustomEnvironment customEnvironment) {
        super(customEnvironment);
    }

    public static PaymentResponse process(CustomEnvironment env, String orderId, String requestId, String amount,
            String orderInfo, String returnURL, String notifyURL, String extraData,
            RequestType requestType, Boolean autoCapture) throws MoMoException {
        try {
            CreateOrderMomo m2Processor = new CreateOrderMomo(env);

            PaymentRequest request = m2Processor.createPaymentCreationRequest(orderId, requestId, amount, orderInfo,
                    returnURL, notifyURL, extraData, requestType, autoCapture);
            // PaymentResponse captureMoMoResponse = m2Processor.execute(request);

            return m2Processor.execute(request);
        } catch (Exception exception) {
            LogUtils.error("[CreateOrderMoMoProcess] " + exception);
        }
        return null;
    }

    @Override
    public PaymentResponse execute(PaymentRequest request) throws RuntimeException {
        try {

            String payload = getGson().toJson(request, PaymentRequest.class);

            HttpResponse response = execute.sendToMoMo(environment.getMomoEndpoint().getCreateUrl(), payload);

            if (response.getStatus() != 200) {
                throw new MoMoException("[PaymentResponse] [" + request.getOrderId() + "] -> Error API");
            }

            System.out.println("uweryei7rye8wyreow8: " + response.getData());

            PaymentResponse captureMoMoResponse = getGson().fromJson(response.getData(), PaymentResponse.class);
            String responserawData = Parameter.REQUEST_ID + "=" + captureMoMoResponse.getRequestId() +
                    "&" + Parameter.ORDER_ID + "=" + captureMoMoResponse.getOrderId() +
                    "&" + Parameter.MESSAGE + "=" + captureMoMoResponse.getMessage() +
                    "&" + Parameter.PAY_URL + "=" + captureMoMoResponse.getPayUrl() +
                    "&" + Parameter.RESULT_CODE + "=" + captureMoMoResponse.getResultCode();

            LogUtils.info("[PaymentMoMoResponse] rawData: " + responserawData);

            return captureMoMoResponse;

        } catch (Exception exception) {
            LogUtils.error("[PaymentMoMoResponse] " + exception);
            throw new IllegalArgumentException("Invalid params capture MoMo Request");
        }
    }

    public PaymentRequest createPaymentCreationRequest(String orderId, String requestId, String amount,
            String orderInfo,
            String returnUrl, String notifyUrl, String extraData, RequestType requestType, Boolean autoCapture) {

        try {
            String requestRawData = new StringBuilder()
                    .append(Parameter.ACCESS_KEY).append("=").append(partnerInfo.getAccessKey()).append("&")
                    .append(Parameter.AMOUNT).append("=").append(amount).append("&")
                    .append(Parameter.EXTRA_DATA).append("=").append(extraData).append("&")
                    .append(Parameter.IPN_URL).append("=").append(notifyUrl).append("&")
                    .append(Parameter.ORDER_ID).append("=").append(orderId).append("&")
                    .append(Parameter.ORDER_INFO).append("=").append(orderInfo).append("&")
                    .append(Parameter.PARTNER_CODE).append("=").append(partnerInfo.getPartnerCode()).append("&")
                    .append(Parameter.REDIRECT_URL).append("=").append(returnUrl).append("&")
                    .append(Parameter.REQUEST_ID).append("=").append(requestId).append("&")
                    .append(Parameter.REQUEST_TYPE).append("=").append(requestType.getRequestType())
                    .toString();

            String signRequest = Encoder.signHmacSHA256(requestRawData, partnerInfo.getSecretKey());
            LogUtils.debug("[PaymentRequest] rawData: " + requestRawData + ", [Signature] -> " + signRequest);

            return new PaymentRequest(partnerInfo.getPartnerCode(), orderId, requestId, Language.EN, orderInfo,
                    Long.valueOf(amount), "test MoMo", null, requestType,
                    returnUrl, notifyUrl, "test store ID", extraData, null, autoCapture, null, signRequest);
        } catch (Exception e) {
            LogUtils.error("[PaymentRequest] " + e);
        }

        return null;
    }
}
