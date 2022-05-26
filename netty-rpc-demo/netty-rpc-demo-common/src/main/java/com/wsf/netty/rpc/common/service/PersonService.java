package com.wsf.netty.rpc.common.service;

import com.wsf.netty.rpc.common.model.Person;

import java.util.List;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
public interface PersonService {

    List<Person> callPerson(String name, Integer num);
}
