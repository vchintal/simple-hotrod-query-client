package org.everythingjboss.jdg.domain;

import java.util.ArrayList;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

import com.google.gson.Gson;

@ProtoDoc("@Indexed")
public class Person {
    private Long id;
    private String firstName;
    private String lastName;
    private ArrayList<String> nicknames;
    
    private int age;

    public Person() {
        
    }
    
    public Person(Long id, String firstName, String lastName, ArrayList<String> nicknames, int age) {
        this.age = age;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nicknames = nicknames;
        this.id = id;
    }
    
    @ProtoDoc("@Field")
    @ProtoField(number = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ProtoDoc("@Field(index=Index.YES, analyze=Analyze.NO)")
    @ProtoField(number = 2)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @ProtoDoc("@Field(index=Index.YES, analyze=Analyze.NO)")
    @ProtoField(number = 3)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @ProtoDoc("@Field(index=Index.YES)")
    @ProtoField(number = 4, required = true)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    public ArrayList<String>  getNicknames() {
        return nicknames;
    }

    @ProtoDoc("@Field(index=Index.YES)")
    @ProtoField(number = 5)
    public void setNicknames(ArrayList<String> nicknames) {
        this.nicknames = nicknames;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

