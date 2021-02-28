package org.geektimes.projects.user.web.controller;

import org.geektimes.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("")
public class RegisterController implements PageController {

    @GET
    @Path("/signin")
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return "signin.jsp";
    }
}
