package org.everythingjboss.jdg.domain;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoDoc("@Indexed")
public class Person {
	private Long id;
	private String firstName;
	private String lastName;
	private int age;

	public Person() {
		
	}
	
	public Person(Long id, String firstName, String lastName, int age) {
		this.age = age;
		this.firstName = firstName;
		this.lastName = lastName;
		this.id = id;
	}
	
	@ProtoDoc("@IndexedField")
	@ProtoField(number = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ProtoDoc("@IndexedField")
	@ProtoField(number = 2)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@ProtoDoc("@IndexedField")
	@ProtoField(number = 3)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@ProtoDoc("@IndexedField")
	@ProtoField(number = 4, required = true)
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	public String toString() {
		return "["+id+","+firstName+","+lastName+","+age+"]";
	}
}

