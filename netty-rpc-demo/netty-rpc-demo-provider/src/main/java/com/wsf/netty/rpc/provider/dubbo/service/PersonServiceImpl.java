package com.wsf.netty.rpc.provider.dubbo.service;

import com.wsf.netty.rpc.common.annotation.NettyRpcService;
import com.wsf.netty.rpc.common.model.Person;
import com.wsf.netty.rpc.common.service.PersonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
@Service
@NettyRpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {

    @Override
    public List<Person> callPerson(String name, Integer num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        return persons;
    }
}
