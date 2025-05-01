package com.zentrix.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zentrix.configuration.CustomEnvironment;
import com.zentrix.configuration.PartnerInfo;
import com.zentrix.model.utils.Execute;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */

public abstract class AbstractProcess<T, V> {

    protected PartnerInfo partnerInfo;
    protected CustomEnvironment environment;
    protected Execute execute = new Execute();

    public AbstractProcess(CustomEnvironment environment) {
        this.environment = environment;
        this.partnerInfo = environment.getPartnerInfo();
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }

    public abstract V execute(T request) throws RuntimeException;
}
