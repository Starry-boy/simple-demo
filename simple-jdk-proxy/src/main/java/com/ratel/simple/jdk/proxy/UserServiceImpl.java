package com.ratel.simple.jdk.proxy;

/**
 * @author ratel
 * @date 2020/8/19
 */
public class UserServiceImpl implements UserService {
	@Override
	public String query(int id) {
		System.out.println("target query method");
		return "query"+id;
	}
}
