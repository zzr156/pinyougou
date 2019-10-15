package com.pinyougou.shop.login;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.ISellerService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;


public class UserServiceimpl implements UserDetailsService {

    private ISellerService sellerService;

    public void setSellerService(ISellerService sellerService) {
        this.sellerService = sellerService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        TbSeller tbSeller = sellerService.findOne(username);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        if(tbSeller!=null){
            if(tbSeller.getStatus().equals("1")){

                return new User(username,tbSeller.getPassword(),grantedAuthorities);
            }else{
                return null;
            }
        }else{
            return null;
        }

    }
}
