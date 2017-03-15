package hello;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Date;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;

/**
 * @author xuch.
 */
@Slf4j
@RestController
public class GreetingController {

    //private Logger log = LoggerFactory.getLogger(GreetingController.class);

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/")
    public String home() {
        return "testindex";
    }

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @RequestMapping("/loggedIn")
    public ModelAndView listGuestbook() {
        UserService userService = UserServiceFactory.getUserService();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return new ModelAndView("redirect:" + userService.createLoginURL("/"));
        }
        else {
            return new ModelAndView("home", "welcomeMsg", "You are authenticated, " + currentUser.getNickname());
        }
    }

    @RequestMapping("/sign")
    public String signGuestbook(
            @RequestParam(required = true, value = "guestbookName") String guestbookName,
            @RequestParam(required = true, value = "content") String content,
            Model model) {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        Key guestbookKey = KeyFactory.createKey("Guestbook", guestbookName);
        Date date = new Date();
        Entity greeting = new Entity("Greeting", guestbookKey);
        greeting.setProperty("user", user);
        greeting.setProperty("date", date);
        greeting.setProperty("content", content);

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        datastoreService.put(greeting);

        model.addAttribute("guestbookName", guestbookName);
        return "home";
    }

}
