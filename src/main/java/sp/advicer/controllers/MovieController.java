package sp.advicer.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;

import sp.advicer.entity.dto.film.Film;
import sp.advicer.repository.TmdbApi;
import sp.advicer.service.MovieService;
import sp.advicer.service.MovieServiceImpl;

@Controller
public class MovieController {
	private MovieService movieService = new MovieServiceImpl();
	private TmdbApi api = new TmdbApi();
	
	@GetMapping(path = "/moviesAdvicer/get/{number}")
	public ModelAndView getSome(@PathVariable("number") Integer number,@RequestParam List<Integer> ids) {
		if (number < 1) throw new RestClientException("Wrong number of films");
		if (ids.isEmpty()) throw new RestClientException("There must be at least one id.");
		List<Integer> films_id =new ArrayList<Integer>( new HashSet<>(ids));
		List<Film> films = getListFilmsByIds(films_id);
		List<Film>recommendFilms =getListFilmsByIds(movieService.getRecomendationList(number,films));
		ModelAndView modelAndView = new ModelAndView("movies");
		modelAndView.addObject("films",recommendFilms);
		return modelAndView;
	}
	
	private List<Film> getListFilmsByIds(List<Integer> ids){
		List<Film> films = new ArrayList<Film>();
		for (Integer id : ids){
			try {
				films.add(api.getMovieById(id));
			} catch (RestClientException e) {
				if (e.getMessage().contains("429")) {
					try {
						TimeUnit.SECONDS.sleep(10);
						films.add(api.getMovieById(id));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return films;
	}
	
	@GetMapping(path = "/moviesAdvicer/main")
	public String mainPage() {
		return "mainPage";
	}
}
