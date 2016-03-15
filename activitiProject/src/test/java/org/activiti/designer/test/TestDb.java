package test.java.org.activiti.designer.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.baimi.activiti.dao.UserDao;
import com.baimi.activiti.entity.User;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-application.xml")
public class TestDb {
	@Resource
	UserDao userDao;
	
	@Test
	public void testQuery(){
		List<User> list=userDao.queryAll();
//		User user=userDao.queryUser("周杰");
		System.out.println(list.get(0));
	}
	
	
	@Test
	public void testAdd(){
		try {
			User u=new User();
			u.setAge(21);
			u.setName("周杰");
			SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
			u.setBirthday(sdf.parse("1996-05-21"));
			userDao.insertUser(u);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Test
	public void testUpdate(){
		User u=new User();
		u.setAge(24);
		u.setId(1);
		userDao.updateUser(u);
		
	}
	@Test
	public void testDelete(){
		User u=new User();
		u.setId(1);
		userDao.deleteUser(u);
		
	}
	
	
}
