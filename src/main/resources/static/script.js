let currentMovie = null;

document.getElementById('searchForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const title = document.getElementById('title').value;
    
    const errorDiv = document.getElementById('error');
    const resultsDiv = document.getElementById('results');
    const loaderDiv = document.getElementById('loader');
    
    errorDiv.style.display = 'none';
    resultsDiv.style.display = 'none';
    loaderDiv.style.display = 'block';
    
    try {
        // Сначала получаем список результатов
        const listUrl = `/api/movies/search-list?title=${encodeURIComponent(title)}`;
        const listResponse = await fetch(listUrl);
        const movies = await listResponse.json();
        
        
        loaderDiv.style.display = 'none';
        
        if (!listResponse.ok) {
            throw new Error('Ошибка при поиске фильма');
        }
        
        // Показываем список результатов
        displayMovieList(movies);
    } catch (error) {
        loaderDiv.style.display = 'none';
        errorDiv.textContent = error.message;
        errorDiv.style.display = 'block';
    }
});

async function loadMovieDetails(imdbId) {
    const loaderDiv = document.getElementById('loader');
    const errorDiv = document.getElementById('error');
    const resultsDiv = document.getElementById('results');
    const listContainer = document.getElementById('movieListContainer');
    
    if (listContainer) {
        listContainer.style.display = 'none';
    }
    
    loaderDiv.style.display = 'block';
    errorDiv.style.display = 'none';
    
    try {
        const response = await fetch(`/api/movies/details?imdbId=${imdbId}`);
        const data = await response.json();
        
        loaderDiv.style.display = 'none';
        
        if (!response.ok) {
            throw new Error(data.message || 'Ошибка при получении деталей фильма');
        }
        
        currentMovie = data;
        displayMovie(data);
    } catch (error) {
        loaderDiv.style.display = 'none';
        errorDiv.textContent = error.message;
        errorDiv.style.display = 'block';
    }
}

function displayMovieList(movies) {
    const resultsDiv = document.getElementById('results');
    let listContainer = document.getElementById('movieListContainer');
    if (!listContainer) {
        listContainer = document.createElement('div');
        listContainer.id = 'movieListContainer';
        listContainer.className = 'movie-list-container';
        listContainer.innerHTML = '<h2>Результаты поиска:</h2><div id="movieList" class="movie-list"></div>';
        resultsDiv.parentNode.insertBefore(listContainer, resultsDiv);
    }
    
    const movieList = document.getElementById('movieList');
    movieList.innerHTML = '';
    
    if (!movies || movies.length === 0) {
        movieList.innerHTML = '<p>Ничего не найдено</p>';
        listContainer.style.display = 'block';
        return;
    }
    
    movies.forEach(movie => {
        const movieItem = document.createElement('div');
        movieItem.className = 'movie-list-item';
        movieItem.innerHTML = `
            <div class="movie-item-content">
                <img src="${movie.poster || ''}" alt="Poster" class="movie-item-poster" onerror="this.style.display='none'">
                <div class="movie-item-info">
                    <h4>${movie.title}</h4>
                    <p><strong>Год:</strong> ${movie.year || 'N/A'}</p>
                    <p><strong>Актеры:</strong> ${movie.actors || 'N/A'}</p>
                </div>
                <button class="view-details-btn" onclick="loadMovieDetails('${movie.imdbId}')">Подробнее</button>
            </div>
        `;
        movieList.appendChild(movieItem);
    });
    
    listContainer.style.display = 'block';
}

function displayMovie(movie) {
    const poster = document.getElementById('poster');
    if (movie.Poster && movie.Poster !== 'N/A') {
        poster.src = movie.Poster;
        poster.style.display = 'block';
    } else {
        poster.style.display = 'none';
    }
    
    document.getElementById('movieTitle').textContent = movie.title;
    document.getElementById('movieYear').textContent = movie.year || 'N/A';
    document.getElementById('movieRating').textContent = movie.imdbRating || 'N/A';
    document.getElementById('movieGenre').textContent = movie.genre || 'N/A';
    document.getElementById('movieDirector').textContent = movie.director || 'N/A';
    document.getElementById('movieActors').textContent = movie.actors || 'N/A';
    document.getElementById('movieRuntime').textContent = movie.runtime || 'N/A';
    document.getElementById('movieRated').textContent = movie.rated || 'N/A';
    document.getElementById('movieWriter').textContent = movie.writer || 'N/A';
    document.getElementById('moviePlot').textContent = movie.plot || 'N/A';
    
    document.getElementById('results').style.display = 'block';
}

async function saveMovie() {
    if (!currentMovie) return;
    
    try {
        const response = await fetch('/api/movies/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(currentMovie)
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Ошибка при сохранении фильма');
        }
        
        alert('Фильм успешно сохранен!');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

async function deleteMovie(id) {
    if (!confirm('Вы уверены, что хотите удалить этот фильм?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/movies/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Ошибка при удалении фильма');
        }
        
        alert('Фильм успешно удален!');
        location.reload();
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}
