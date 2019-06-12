package org.hum.nettyproxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hum.nettyproxy.common.enumtype.RunModeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class ServerRunArg {

	// nettyproxy.runmode=11
	private RunModeEnum runMode;
	// nettyproxy.port=5432
	private int port;
	// nettyproxy.workercnt=80
	private int workerCnt;
	// nettyproxy.outside_proxy_host=57.12.39.152
	private String outsidePorxyHost;
	// nettyproxy.outside_proxy_port=5432
	private int outsideProxyPort;
	
	public ServerRunArg() { }
	public ServerRunArg(RunModeEnum runMode, int port) {
		this.runMode = runMode;
		this.port = port;
	}
}
class ServerRunArgParser {

	private static final Logger logger = LoggerFactory.getLogger(ServerRunArgParser.class);
	private static final String NETTY_PROXY_ARGS_PREFIX = "nettyproxy.";
	private static final String RUNMODE_KEY = "runmode";
	private static final String PORT_KEY = "port";
	private static final String WORKER_CNT_KEY = "workercnt";
	private static final String OUTSIDE_PROXY_HOST_KEY = "outside_proxy_host";
	private static final String OUTSIDE_PROXY_PORT_KEY = "outside_proxy_port";
	
	private static final int DEFAULT_LISTENNING_PORT = 52996;
	private static final int DEFAULT_WORKER_CNT = Runtime.getRuntime().availableProcessors() * 10;
	private static final ServerRunArg DEFAULT_SERVER_RUN_ARGS = new ServerRunArg(RunModeEnum.HttpSimpleProxy, DEFAULT_LISTENNING_PORT);
	
	public static ServerRunArg toServerRunArg(String args[]) {
		logger.debug("prepare parse args, input_args=" + Arrays.toString(args));
		
		Map<String, String> paramMap = toMap(args);
		if (paramMap == null || paramMap.isEmpty()) {
			// 使用默认启动参数
			logger.info("no args found, use default param=" + DEFAULT_SERVER_RUN_ARGS);
			return DEFAULT_SERVER_RUN_ARGS;
		}

		RunModeEnum runMode = RunModeEnum.getEnum(parseInt(paramMap.get(RUNMODE_KEY), "param \"runmode\" is invaild"));
		
		ServerRunArg serverRunArgs = new ServerRunArg();
		serverRunArgs.setRunMode(runMode);
		serverRunArgs.setPort(parseInt(paramMap.get(PORT_KEY), "param \"port\" is invaild"));
		serverRunArgs.setWorkerCnt(paramMap.containsKey(WORKER_CNT_KEY)? parseInt(paramMap.get(PORT_KEY), "param \"workercnt\" is invaild") : DEFAULT_WORKER_CNT);
		
		if (runMode == RunModeEnum.HttpInsideServer || runMode == RunModeEnum.SocksInsideServer) { 
			String outsideProxyHost = paramMap.get(OUTSIDE_PROXY_HOST_KEY);
			if (outsideProxyHost == null || outsideProxyHost.isEmpty()) {
				throw new IllegalArgumentException("param \"outside_proxy_host\" is invaild");
			}
			serverRunArgs.setOutsidePorxyHost(outsideProxyHost);
			serverRunArgs.setOutsideProxyPort(parseInt(paramMap.get(OUTSIDE_PROXY_PORT_KEY), "param \"outside_proxy_port\" is invaild"));
			// TODO 检测Proxy是否可达
		}
		
		return serverRunArgs;
	}
	
	public static int parseInt(String str, String message) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException ce) {
			throw new IllegalArgumentException(message, ce);
		}
	}
	
	public static Map<String, String> toMap(String args[]) {
		if (args == null || args.length == 0) {
			return Collections.emptyMap();
		}
		Map<String, String> paramMap = new HashMap<String, String>();
		for (String arg : args) {
			if (arg.startsWith(NETTY_PROXY_ARGS_PREFIX)) {
				arg = arg.replace(NETTY_PROXY_ARGS_PREFIX, ""); // delete prefix
				int splitIndex = arg.indexOf("=");
				if (splitIndex <= 0) {
					logger.warn("found invaild param=" + arg);
					throw new IllegalArgumentException("found invaild param=" + arg);
				}
				String key = arg.substring(0, splitIndex);
				String value = arg.substring(splitIndex + 1, arg.length());
				paramMap.put(key, value);
				logger.info("parse arg success, key={}, value={}", key, value);
			}
		}
		return paramMap;
	}
}
