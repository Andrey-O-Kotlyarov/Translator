package testgroup.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import testgroup.model.Film;
import testgroup.service.FilmService;
import testgroup.service.FilmServiceImpl;
import java.util.List;

@Controller
public class FilmController {

    private static Film film;

    @Autowired
    private FilmService filmService;     
    
    @RequestMapping(value = "/film", method = RequestMethod.GET)
    public ModelAndView allFilms() {

        List<Film> films = filmService.allFilms();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("films");
        modelAndView.addObject("filmsList", films);
        return modelAndView;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public ModelAndView addPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("addPage");
        return modelAndView;
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") int id) {

        Film film = filmService.getById(id);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("editPage");
        modelAndView.addObject("film", film);
        return modelAndView;
    }

    @RequestMapping(value="/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteFilm(@PathVariable("id") int id) {

        Film film = filmService.getById(id);
        filmService.delete(film);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/film");
        return modelAndView;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ModelAndView editFilm(@ModelAttribute("film") Film film) {

        filmService.edit(film);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/film");
        return modelAndView;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ModelAndView addFilm(@ModelAttribute("film") Film film) {

        filmService.add(film);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/film");
        return modelAndView;
    }
}