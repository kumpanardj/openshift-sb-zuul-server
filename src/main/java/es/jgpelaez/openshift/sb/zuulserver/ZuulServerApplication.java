package es.jgpelaez.openshift.sb.zuulserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.ZuulProxyConfiguration;
import org.springframework.cloud.netflix.zuul.filters.discovery.PatternServiceRouteMapper;
import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;
import org.springframework.cloud.netflix.zuul.filters.discovery.SimpleServiceRouteMapper;
import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import com.github.mthizo247.cloud.netflix.zuul.web.socket.ZuulWebSocketConfiguration;

import es.jgpelaez.openshift.sb.zuulserver.config.DynamicZuulConfig;
import es.jgpelaez.openshift.sb.zuulserver.config.WebSocketConfiguration;

@SpringBootApplication(excludeName= { "discoveryClient"})
@EnableCircuitBreaker
/**
 * @author Juan Carlos García Peláez Zuul cannot be import
 *         with @EnableZuulProxy, as this configuration should be loaded later
 */
@EnableZuulProxy
//@Import({ ZuulProxyConfiguration.class, WebSocketConfiguration.class })
public class ZuulServerApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ZuulServerApplication.class).web(true).run(args);
	}

	@Autowired
	private DynamicZuulConfig config;

	@Bean
	public ServiceRouteMapper serviceRouteMapper() {
		if (config.isUseEurekaServices()) {
			return new PatternServiceRouteMapper("(?<name>)", config.getServicesPrefix() + "/${name}");
		} else {
			return new SimpleServiceRouteMapper();
		}
	}

	@Bean
	public ZuulFallbackProvider zuulFallbackProvider() {
		return new ZuulFallbackProvider() {
			@Override
			public String getRoute() {
				return "zuulserver";
			}

			@Override
			public ClientHttpResponse fallbackResponse() {
				return new ClientHttpResponse() {
					@Override
					public HttpStatus getStatusCode() throws IOException {
						return HttpStatus.OK;
					}

					@Override
					public int getRawStatusCode() throws IOException {
						return 200;
					}

					@Override
					public String getStatusText() throws IOException {
						return "OK";
					}

					@Override
					public void close() {

					}

					@Override
					public InputStream getBody() throws IOException {
						return new ByteArrayInputStream("fallback".getBytes());
					}

					@Override
					public HttpHeaders getHeaders() {
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						return headers;
					}
				};
			}
		};
	}

}
