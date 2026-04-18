package com.ggg.rememo.feature.auth.contract;

import com.ggg.rememo.core.base.IBaseView;

public interface AuthContract {
    interface View extends IBaseView {
        /**
         * 显示登录成功
         * @param userId 登录用户的 ID
         */
        void showLoginSuccess(String userId);

        /**
         * 显示注册成功
         * @param userId 注册用户的 ID
         */
        void showRegisterSuccess(String userId);

        /** @return 登录手机号 */
        String getLoginPhone();

        /** @return 登录密码 */
        String getLoginPassword();

        /** @return 注册手机号 */
        String getRegisterPhone();

        /** @return 注册验证码 */
        String getRegisterCode();

        /** @return 注册密码 */
        String getRegisterPassword();

        /** @return 是否勾选用户协议 */
        boolean isAgreementChecked();

        void showLoginCountDown();
        void showRegisterCountDown();
    }

    interface Presenter {
        /**
         * 执行密码登录
         */
        void login();

        /**
         * 执行注册
         */
        void register();

        /**
         * 请求登录验证码（手机号+验证码登录模式）
         */
        void requestLoginCode();

        /**
         * 请求注册验证码
         */
        void requestRegisterCode();
    }
}
