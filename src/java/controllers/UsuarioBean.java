/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controllers;

import java.io.UnsupportedEncodingException;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import libs.BuildHash;
import libs.BuildMail;
import libs.BuildMessage;
import models.entities.Usuario;
import models.persistence.UsuarioDao;

/**
 *
 * @author smp
 */

@ManagedBean
public class UsuarioBean {
    
    private Usuario usuario;
    private UsuarioDao usuarioDao;
    
    public UsuarioBean()
    {
        this.usuario = new Usuario();
        this.usuarioDao =  new UsuarioDao();
    }
    
    public Usuario  getUsuario()
    {
        return this.usuario;
    }
    
    public void save(Usuario usuario)
   {
       BuildMessage buildMessage = new BuildMessage();
       try {
           
           BuildHash buildHash = new BuildHash();  
           String hashMail = buildHash.createHash(usuario.getEmail());
           usuario.setHashmail(hashMail);
           this.usuarioDao.save(usuario);
           
           BuildMail buildMail =  new BuildMail();
           buildMail.sendRegisterNotification(
                                        usuario.getEmail()
                                       ,usuario.getNome()
                                       ,hashMail
                                       );
           
           buildMessage.addInfo();
           
           this.usuario =  new Usuario();
          
       } catch (Exception e) {
           buildMessage.addError("Email já cadastrado");
           System.out.println("error: "+ e.getMessage());
           e.printStackTrace();  
       }
   }
    
    public String authenticator(Usuario usuario)
    {
        BuildMessage  buildMessage = new BuildMessage();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        try{
            BuildHash buildHash = new BuildHash();
            usuario.setSenha(buildHash.createHash(usuario.getSenha()));
            this.usuario = this.usuarioDao.selectUsuarioByEmailAndSenha(usuario);
            
            if(this.usuario != null){
                ExternalContext externalContext = facesContext.getExternalContext();
                HttpSession session = (HttpSession) externalContext.getSession(false);
                session.setAttribute("usuario", this.usuario);
                
                return "/user/inicio.xhtml";
                
            }else{
                buildMessage.addError("Email ou senha inválidos");
                return "/user/login.xhtml";
            }
            
                
        }catch(UnsupportedEncodingException error){
             buildMessage.addError("Email ou senha inválidos");
             return "/user/login.xhtml";
        }
    }
    
    public String exit()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpSession session = (HttpSession) externalContext.getSession(false);
        session.removeAttribute("usuario");
        return "login.xhtml";
    }
}
