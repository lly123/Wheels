package com.freeroom.web.example;

import com.freeroom.web.Apollo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import static org.eclipse.jetty.servlet.ServletContextHandler.*;

public class TestServer
{
    public static void main(final String[] args) throws Exception
    {
        final Server server = new Server(8080);
        final ServletContextHandler context = new ServletContextHandler(SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new Apollo("com.freeroom.web.example.beans")), "/*");

        server.start();
        server.join();
    }
}
