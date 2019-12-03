package com.example.dubbo.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.dubbo.DemoService;

/**
 * 示例接口实现类
 * DemoServiceImpl
 */
@Service
public class DemoServiceImpl implements DemoService {
	// 示例方法的实现
	public String sayHello(String name) {
		System.out.println("*********************** " + name);
		return "Hello " + name;
	}
}
