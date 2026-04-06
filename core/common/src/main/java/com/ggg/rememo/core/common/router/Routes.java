package com.ggg.rememo.core.common.router;

/**
 * 路由常量集中管理。
 *
 * <p>目的：</p>
 * <ul>
 *   <li>避免在代码里散落硬编码字符串（"/here/home" 这种容易写错、难以全局替换）。</li>
 *   <li>跨模块导航时，调用方只依赖 core:common，不需要依赖目标 feature 的具体类。</li>
 *   <li>便于后续做统一拦截（登录校验、埋点、降级）以及重构路径。</li>
 * </ul>
 *
 * <p>命名约定：</p>
 * <ul>
 *   <li>统一格式：/模块名/页面名（例如：/here/home）</li>
 *   <li>模块名使用 feature 模块名一致的短名：here/explore/timeline/publish/profile</li>
 *   <li>页面名建议使用 home/detail/edit/list 等，不要用中文</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *   <li>Routes 只放“路径常量”，不要放导航逻辑。</li>
 *   <li>如果未来替换路由框架（非 ARouter），这里只需要替换使用方的实现，常量仍可复用。</li>
 * </ul>
 */
public final class Routes {

    private Routes() {
        // 工具类禁止实例化
        throw new AssertionError("No instances.");
    }

    /** 首页（地图/在此）相关路由 */
    public static final class Here {
        private Here() { throw new AssertionError("No instances."); }

        public static final String HOME = "/here/home";
        public static final String HOME_FRAGMENT = "/here/home_fragment";
        public static final String DETAIL = "/here/detail";
        public static final String AI_CHAT = "/here/ai_chat";
    }

    /** 探索页相关路由 */
    public static final class Explore {
        private Explore() { throw new AssertionError("No instances."); }

        public static final String HOME = "/explore/home";
        public static final String HOME_FRAGMENT = "/explore/home_fragment";
    }

    /** 时序（时间线）相关路由 */
    public static final class Timeline {
        private Timeline() { throw new AssertionError("No instances."); }

        public static final String HOME = "/timeline/home";
        public static final String HOME_FRAGMENT = "/timeline/home_fragment";
        public static final String YEAR_ARCHIVE = "/timeline/year_archive";

        /** Timeline 模块 Intent 参数 Key */
        public static final String EXTRA_POINT_ID = "extra_point_id";
        public static final String EXTRA_YEAR = "extra_year";
    }

    /** 消息(Message)相关路由 */
    public static final class Message {
        private Message() { throw new AssertionError("No instances."); }

        public static final String HOME = "/message/home";
        public static final String HOME_FRAGMENT = "/message/home_fragment";
    }

    /** 发布相关路由 */
    public static final class Publish {
        private Publish() { throw new AssertionError("No instances."); }

        public static final String HOME = "/publish/home";
        public static final String EXTRA_POINT_ID = "point_id";
        public static final String EXTRA_LAT = "lat";
        public static final String EXTRA_LNG = "lng";
        public static final String EXTRA_ADDRESS = "address";
        public static final String EXTRA_POINT_NAME = "point_name";
    }

    /** 我的（Profile）相关路由 */
    public static final class Profile {
        private Profile() { throw new AssertionError("No instances."); }

        public static final String HOME = "/profile/home";
        public static final String HOME_FRAGMENT = "/profile/home_fragment";
        public static final String USER_SERVICE = "/profile/user_service";
    }

    /** 地图相关路由 */
    public static final class Map {
        private Map() { throw new AssertionError("No instances."); }

        public static final String LOCATION_PICKER = "/map/location_picker";
    }

    /** 记忆详情相关路由 */
    public static final class Detail {
        private Detail() {
            throw new AssertionError("No instances.");
        }
        public static final String HOME = "/detail/home";
        public static final String EXTRA_POST_ID = "postId";
    }

    /** 登录注册相关路由 */
    public static final class Auth {
        private Auth() { throw new AssertionError("No instances."); }

        public static final String LOGIN = "/auth/login";
        public static final String REGISTER = "/auth/register";
    }

    /** 主页面相关路由 */
    public static final class Main {
        private Main() { throw new AssertionError("No instances."); }

        public static final String HOME = "/main/home";
    }
}