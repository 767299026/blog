package blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.function.Predicate;

@Configuration
public class Swagger3Config implements WebMvcConfigurer {

    @Bean
    public Docket docket(Environment environment) {

        /*//设置要显示的swagger环境
        Profiles profiles = Profiles.of();
        //通过environment.acceptsProfiles判断是否处在自己设定的环境当中
        boolean flag=environment.acceptsProfiles(profiles);*/

        return new Docket(DocumentationType.OAS_30)
                //判断是否启用
                //.enable(flag)
                .select()
                //通过apis方法配置要扫描的controller的位置
                .apis(RequestHandlerSelectors.basePackage("blog.controller"))
                //通过paths方法配置路径
                .paths(PathSelectors.any())
                //设置需要排除的路径(如果需要)
                .paths(Predicate.not(PathSelectors.regex("/error.*")))
                .build().apiInfo(new ApiInfoBuilder()
                        //设置文档标题
                        .description("不负明天的自己")
                        //设置联系人信息
                        .contact(new Contact("API作者", null, "lsl767299026@gmail.com"))
                        //设置版本号
                        .version("1.0")
                        //设置文档抬头
                        .title("YIQU的个人博客文档")
                        //设置授权
                        .license("License By YIQU")
                        //设置授权访问网址
                        .licenseUrl("https://swagger.io")
                        .build());
    }
}
