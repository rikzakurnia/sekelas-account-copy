package id.ac.ui.cs.advprog.serviceaccounts.dto;

public class EditProfileRequest {
    private String username;
    private String name;
    private String email;

    public String getUsername(){
        return username;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public void setUsername(String newUsername) {
        username = newUsername;
    }

    public void setEmail(String newEmail){
        email = newEmail;
    }

    public void setName(String newName){
        name = newName;
    }
}
