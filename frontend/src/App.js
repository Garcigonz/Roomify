import { useState, useEffect } from 'react';
import { AlertCircle, Check, LogIn, UserPlus, Home, DoorOpen, Users, Calendar, ChevronLeft, ChevronRight, Trash2, Clock, X, PlusCircle } from 'lucide-react';

const API_URL = 'http://localhost:8080';

// 1. Función auxiliar para decodificar el JWT (Payload)
const parseJwt = (token) => {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
};

function App() {
    const [view, setView] = useState('login');
    const [dashboardView, setDashboardView] = useState('salas');
    const [authToken, setAuthToken] = useState(null);
    const [user, setUser] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Estados para salas
    const [salas, setSalas] = useState([]);
    const [salasPage, setSalasPage] = useState(0);
    const [salasTotalPages, setSalasTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    // Estados para reservas
    const [misReservas, setMisReservas] = useState([]);
    const [loadingReservas, setLoadingReservas] = useState(false);

    // Estados para formularios
    const [loginData, setLoginData] = useState({ id: '', password: '' });
    const [registerData, setRegisterData] = useState({
        id: '',
        nombre: '',
        email: '',
        password: '',
        habitacion: '',
        telefono: '',
        nacimiento: ''
    });

    const [salaSeleccionada, setSalaSeleccionada] = useState(null);

    const isAdmin = user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN' || (Array.isArray(user?.role) && user.role.includes('ROLE_ADMIN'));

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const response = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(loginData),
                credentials: 'include'
            });

            if (response.ok) {
                const authHeader = response.headers.get('Authorization');
                if (authHeader) {
                    const token = authHeader.replace('Bearer ', '');
                    const decodedToken = parseJwt(token);
                    const userRole = decodedToken?.role || decodedToken?.roles || decodedToken?.authorities || 'USER';

                    setAuthToken(token);
                    setUser({ id: loginData.id, role: userRole });
                    setSuccess('¡Inicio de sesión exitoso!');
                    setTimeout(() => setView('dashboard'), 1000);
                }
            } else if (response.status === 401) {
                setError('Credenciales incorrectas');
            } else {
                setError('Error al iniciar sesión');
            }
        } catch (err) {
            setError('Error de conexión con el servidor');
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            const response = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ...registerData,
                    habitacion: parseInt(registerData.habitacion),
                    telefono: registerData.telefono ? parseInt(registerData.telefono) : null
                })
            });

            if (response.status === 201) {
                setSuccess('¡Registro exitoso! Ahora puedes iniciar sesión');
                setTimeout(() => {
                    setView('login');
                    setLoginData({ id: registerData.id, password: '' });
                }, 2000);
            } else if (response.status === 409) {
                setError('El usuario ya existe');
            } else {
                const data = await response.json();
                setError(data.message || 'Error al registrar usuario');
            }
        } catch (err) {
            setError('Error de conexión con el servidor');
        }
    };

    const handleLogout = async () => {
        try {
            await fetch(`${API_URL}/auth/logout`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${authToken}` },
                credentials: 'include'
            });
        } catch (err) {
            console.error('Error al cerrar sesión:', err);
        }
        setAuthToken(null);
        setUser(null);
        setView('login');
        setDashboardView('salas');
        setLoginData({ id: '', password: '' });
    };

    // 🔴 RESTAURADA: Función inteligente para tokens y refresh
    const authenticatedFetch = async (endpoint, options = {}) => {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
            'Authorization': `Bearer ${authToken}`
        };

        let response = await fetch(`${API_URL}${endpoint}`, { ...options, headers });

        if (response.status === 401) {
            console.log("Token caducado. Intentando refrescar...");
            try {
                const refreshResponse = await fetch(`${API_URL}/auth/refresh`, {
                    method: 'POST',
                    credentials: 'include'
                });

                if (refreshResponse.ok) {
                    const newAuthHeader = refreshResponse.headers.get('Authorization');
                    const newToken = newAuthHeader.replace('Bearer ', '');
                    setAuthToken(newToken);

                    // Reintentar petición original
                    headers['Authorization'] = `Bearer ${newToken}`;
                    response = await fetch(`${API_URL}${endpoint}`, { ...options, headers });
                } else {
                    handleLogout();
                    throw new Error("Sesión expirada");
                }
            } catch (error) {
                handleLogout();
                throw error;
            }
        }
        return response;
    };

    const handleCrearReserva = async (e, formData) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const payload = {
                sala: { id: salaSeleccionada.id },
                usuario: { id: user.id },
                horaInicio: formData.horaInicio,
                horaFin: null,
                observaciones: formData.observaciones
            };

            // Usamos authenticatedFetch
            const response = await authenticatedFetch(`/reservas`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            if (response.status === 201) {
                setSuccess('¡Reserva creada con éxito! (Duración: 3h)');
                setSalaSeleccionada(null);
                fetchMisReservas();
                fetchSalas(salasPage);
                setTimeout(() => setSuccess(''), 3000);
            } else if (response.status === 409) {
                setError('Conflicto: Sala ocupada o usuario castigado.');
            } else {
                setError('Error al crear la reserva.');
            }
        } catch (err) {
            setError('Error de conexión con el servidor');
        }
    };

    const handleAmpliarReserva = async (idReserva) => {
        const input = window.prompt("¿Cuántas horas quieres ampliar la reserva?");
        if (!input) return;

        const horas = parseInt(input);
        if (isNaN(horas) || horas <= 0) {
            setError("Por favor, introduce un número válido de horas.");
            return;
        }

        setError('');
        setSuccess('');

        try {
            const response = await authenticatedFetch(`/reservas/${idReserva}/ampliar?horas=${horas}`, {
                method: 'PUT'
            });

            if (response.ok) {
                setSuccess(`¡Reserva ampliada ${horas} hora(s) correctamente!`);
                fetchMisReservas();
                setTimeout(() => setSuccess(''), 3000);
            } else if (response.status === 409) {
                setError('No se puede ampliar: La sala está reservada por otra persona justo después.');
            } else {
                setError('Error al intentar ampliar la reserva.');
            }
        } catch (err) {
            setError('Error de conexión con el servidor.');
        }
    };

    const fetchSalas = async (page = 0) => {
        setLoading(true);
        setError('');
        try {
            // Usamos authenticatedFetch
            const response = await authenticatedFetch(`/salas?page=${page}&size=9&sort=id`);

            if (response.ok) {
                const data = await response.json();
                setSalas(data.content || []);
                setSalasTotalPages(data.totalPages || 0);
                setSalasPage(data.number || 0);
            } else {
                const errorText = await response.text();
                setError(`Error al cargar las salas (${response.status}): ${errorText}`);
            }
        } catch (err) {
            setError(`Error de conexión: ${err.message}`);
        }
        setLoading(false);
    };

    const fetchMisReservas = async () => {
        if (!user?.id) return;

        setLoadingReservas(true);
        setError('');
        try {
            // Usamos authenticatedFetch
            const response = await authenticatedFetch(`/reservas/usuario/${user.id}`);

            if (response.ok) {
                const data = await response.json();
                setMisReservas(data);
            } else {
                if (response.status !== 401) setError('No se pudieron cargar tus reservas.');
            }
        } catch (err) {
            if (err.message !== "Sesión expirada") setError(`Error de conexión: ${err.message}`);
        }
        setLoadingReservas(false);
    };

    const handleCancelarReserva = async (idReserva) => {
        if (!window.confirm('¿Seguro que quieres cancelar esta reserva?')) return;

        try {
            // Usamos authenticatedFetch
            const response = await authenticatedFetch(`/reservas/${idReserva}`, {
                method: 'DELETE'
            });

            if (response.ok || response.status === 204) {
                setSuccess('Reserva cancelada correctamente');
                fetchMisReservas();
                setTimeout(() => setSuccess(''), 3000);
            } else {
                setError('No se pudo cancelar la reserva');
            }
        } catch (err) {
            setError('Error al conectar con el servidor');
        }
    };

    useEffect(() => {
        if (authToken && view === 'dashboard') {
            if (dashboardView === 'salas') {
                fetchSalas(0);
            } else if (dashboardView === 'reservas') {
                fetchMisReservas();
            }
        }
    }, [authToken, view, dashboardView]);

    useEffect(() => {
        setError('');
        setSuccess('');
    }, [view, dashboardView]);

    if (view === 'dashboard') {
        return (
            <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
                <nav className="bg-white shadow-md">
                    <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
                        <div className="flex items-center gap-2">
                            <Home className="w-6 h-6 text-indigo-600" />
                            <h1 className="text-2xl font-bold text-gray-800">Roomify</h1>
                        </div>
                        <div className="flex items-center gap-4">
                            <span className="text-gray-700">
                                Hola, <span className="font-semibold">{user?.id}</span>
                            </span>
                            <button
                                onClick={handleLogout}
                                className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                            >
                                Cerrar Sesión
                            </button>
                        </div>
                    </div>
                </nav>

                <div className="bg-white border-b">
                    <div className="max-w-7xl mx-auto px-4">
                        <div className="flex gap-1">
                            <button
                                onClick={() => setDashboardView('salas')}
                                className={`flex items-center gap-2 px-6 py-4 font-medium transition ${
                                    dashboardView === 'salas'
                                        ? 'text-indigo-600 border-b-2 border-indigo-600'
                                        : 'text-gray-600 hover:text-gray-800'
                                }`}
                            >
                                <DoorOpen className="w-5 h-5" />
                                Salas
                            </button>
                            <button
                                onClick={() => setDashboardView('reservas')}
                                className={`flex items-center gap-2 px-6 py-4 font-medium transition ${
                                    dashboardView === 'reservas'
                                        ? 'text-indigo-600 border-b-2 border-indigo-600'
                                        : 'text-gray-600 hover:text-gray-800'
                                }`}
                            >
                                <Calendar className="w-5 h-5" />
                                Reservas
                            </button>
                            {isAdmin && (
                                <button
                                    onClick={() => setDashboardView('usuarios')}
                                    className={`flex items-center gap-2 px-6 py-4 font-medium transition ${
                                        dashboardView === 'usuarios'
                                            ? 'text-indigo-600 border-b-2 border-indigo-600'
                                            : 'text-gray-600 hover:text-gray-800'
                                    }`}
                                >
                                    <Users className="w-5 h-5" />
                                    Usuarios
                                </button>
                            )}
                        </div>
                    </div>
                </div>

                <div className="max-w-7xl mx-auto px-4 py-8">
                    {dashboardView === 'salas' && (
                        <div>
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-3xl font-bold text-gray-800">Salas Disponibles</h2>
                                <div className="text-sm text-gray-600">
                                    Mostrando <span className="font-semibold">{salas.length}</span> salas
                                </div>
                            </div>

                            {error && (
                                <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2">
                                    <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                                    <p className="text-red-800 text-sm">{error}</p>
                                </div>
                            )}

                            {loading ? (
                                <div className="text-center py-12">
                                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
                                    <p className="mt-4 text-gray-600">Cargando salas...</p>
                                </div>
                            ) : (
                                <>
                                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                        {salas.map((sala) => (
                                            <div key={sala.id} className="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition">
                                                <div className="flex items-start justify-between mb-4">
                                                    <div className="flex items-center gap-3">
                                                        <div className="p-3 bg-indigo-100 rounded-lg">
                                                            <DoorOpen className="w-6 h-6 text-indigo-600" />
                                                        </div>
                                                        <div>
                                                            <h3 className="text-xl font-bold text-gray-800">{sala.descripcion}</h3>
                                                            <p className="text-sm text-gray-500">ID: {sala.id}</p>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="space-y-3">
                                                    <div className="flex items-center gap-2 text-gray-700">
                                                        <Users className="w-4 h-4 text-gray-400" />
                                                        <span className="text-sm">Aforo: <span className="font-semibold">{sala.aforo} personas</span></span>
                                                    </div>

                                                    {sala.responsableActual && (
                                                        <div className="pt-3 border-t">
                                                            <p className="text-xs text-gray-500">Responsable actual:</p>
                                                            <p className="text-sm font-medium text-gray-700">{sala.responsableActual.nombre}</p>
                                                        </div>
                                                    )}
                                                </div>

                                                <button
                                                    onClick={() => setSalaSeleccionada(sala)}
                                                    className="w-full mt-4 bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 transition">
                                                    Reservar
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                    {/* Paginación */}
                                    {salasTotalPages > 1 && (
                                        <div className="flex justify-center items-center gap-4 mt-8">
                                            <button
                                                onClick={() => fetchSalas(salasPage - 1)}
                                                disabled={salasPage === 0}
                                                className="p-2 rounded-lg bg-white shadow hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                            >
                                                <ChevronLeft className="w-5 h-5" />
                                            </button>
                                            <span className="text-gray-700 font-medium">Página {salasPage + 1} de {salasTotalPages}</span>
                                            <button
                                                onClick={() => fetchSalas(salasPage + 1)}
                                                disabled={salasPage >= salasTotalPages - 1}
                                                className="p-2 rounded-lg bg-white shadow hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                            >
                                                <ChevronRight className="w-5 h-5" />
                                            </button>
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    )}

                    {dashboardView === 'reservas' && (
                        <div>
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-3xl font-bold text-gray-800">Mis Reservas</h2>
                            </div>

                            {error && (
                                <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2">
                                    <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                                    <p className="text-red-800 text-sm">{error}</p>
                                </div>
                            )}
                            {success && (
                                <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-2">
                                    <Check className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                                    <p className="text-green-800 text-sm">{success}</p>
                                </div>
                            )}

                            {loadingReservas ? (
                                <div className="text-center py-12">
                                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
                                    <p className="mt-4 text-gray-600">Cargando tus reservas...</p>
                                </div>
                            ) : misReservas.length === 0 ? (
                                <div className="bg-white rounded-lg shadow p-8 text-center">
                                    <Calendar className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                                    <h3 className="text-lg font-medium text-gray-900">No tienes reservas activas</h3>
                                    <p className="text-gray-500 mt-2">Ve a la sección de Salas para realizar una nueva reserva.</p>
                                    <button
                                        onClick={() => setDashboardView('salas')}
                                        className="mt-4 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                                    >
                                        Ver Salas
                                    </button>
                                </div>
                            ) : (
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                    {misReservas.map((reserva) => {
                                        const inicio = new Date(reserva.horaInicio);
                                        const fin = new Date(reserva.horaFin);

                                        return (
                                            <div key={reserva.id} className="bg-white rounded-lg shadow-lg p-6 border-l-4 border-indigo-500 hover:shadow-xl transition">
                                                <div className="flex justify-between items-start mb-4">
                                                    <div>
                                                        <h3 className="text-lg font-bold text-gray-800">
                                                            {reserva.sala?.descripcion || "Sala desconocida"}
                                                        </h3>
                                                        <span className="text-xs bg-indigo-100 text-indigo-800 px-2 py-1 rounded-full mt-1 inline-block">
                                                            ID: {reserva.id.substring(reserva.id.length - 6)}...
                                                        </span>
                                                    </div>

                                                    <div className="flex gap-2">
                                                        <button
                                                            onClick={() => handleAmpliarReserva(reserva.id)}
                                                            className="text-blue-600 hover:text-blue-800 p-2 hover:bg-blue-50 rounded-full transition"
                                                            title="Ampliar duración"
                                                        >
                                                            <PlusCircle className="w-5 h-5" />
                                                        </button>

                                                        <button
                                                            onClick={() => handleCancelarReserva(reserva.id)}
                                                            className="text-red-500 hover:text-red-700 p-2 hover:bg-red-50 rounded-full transition"
                                                            title="Cancelar reserva"
                                                        >
                                                            <Trash2 className="w-5 h-5" />
                                                        </button>
                                                    </div>
                                                </div>

                                                <div className="space-y-3">
                                                    <div className="flex items-center gap-3 text-gray-700">
                                                        <Calendar className="w-4 h-4 text-gray-400" />
                                                        <span className="text-sm font-medium">
                                                            {inicio.toLocaleDateString()}
                                                        </span>
                                                    </div>

                                                    <div className="flex items-center gap-3 text-gray-700">
                                                        <Clock className="w-4 h-4 text-gray-400" />
                                                        <span className="text-sm">
                                                            {inicio.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -
                                                            {fin.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                        </span>
                                                    </div>

                                                    {reserva.observaciones && (
                                                        <div className="mt-3 p-3 bg-gray-50 rounded text-sm text-gray-600 italic">
                                                            "{reserva.observaciones}"
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    )}

                    {dashboardView === 'usuarios' && isAdmin && (
                        <div className="bg-white rounded-lg shadow-lg p-8">
                            <h2 className="text-3xl font-bold text-gray-800 mb-4">Gestión de Usuarios</h2>
                            <p className="text-gray-600">Próximamente...</p>
                        </div>
                    )}
                </div>
                {/* MODAL DE RESERVA */}
                {salaSeleccionada && (
                    <ModalReserva
                        sala={salaSeleccionada}
                        onClose={() => setSalaSeleccionada(null)}
                        onSubmit={handleCrearReserva}
                    />
                )}
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
            {/* ... Login/Register ... */}
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-indigo-600 rounded-full mb-4">
                        <Home className="w-8 h-8 text-white" />
                    </div>
                    <h1 className="text-4xl font-bold text-gray-800 mb-2">Roomify</h1>
                    <p className="text-gray-600">Sistema de gestión de salas</p>
                </div>
                <div className="bg-white rounded-2xl shadow-xl p-8">
                    <div className="flex gap-2 mb-6">
                        <button
                            onClick={() => { setView('login'); setError(''); setSuccess(''); }}
                            className={`flex-1 py-3 rounded-lg font-semibold transition ${view === 'login' ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
                        >
                            <LogIn className="w-4 h-4 inline mr-2" /> Iniciar Sesión
                        </button>
                        <button
                            onClick={() => { setView('register'); setError(''); setSuccess(''); }}
                            className={`flex-1 py-3 rounded-lg font-semibold transition ${view === 'register' ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
                        >
                            <UserPlus className="w-4 h-4 inline mr-2" /> Registrarse
                        </button>
                    </div>

                    {error && (<div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2"><AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" /><p className="text-red-800 text-sm">{error}</p></div>)}
                    {success && (<div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-2"><Check className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" /><p className="text-green-800 text-sm">{success}</p></div>)}

                    {view === 'login' && (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Usuario</label>
                                <input type="text" value={loginData.id} onChange={(e) => setLoginData({ ...loginData, id: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" placeholder="Tu ID de usuario" />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Contraseña</label>
                                <input type="password" value={loginData.password} onChange={(e) => setLoginData({ ...loginData, password: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" placeholder="Tu contraseña" onKeyDown={(e) => e.key === 'Enter' && handleLogin(e)} />
                            </div>
                            <button onClick={handleLogin} className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition">Iniciar Sesión</button>
                        </div>
                    )}
                    {view === 'register' && (
                        <div className="space-y-4">
                            <div><label className="block text-sm font-medium text-gray-700 mb-2">ID de Usuario *</label><input type="text" value={registerData.id} onChange={(e) => setRegisterData({ ...registerData, id: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" /></div>
                            <div><label className="block text-sm font-medium text-gray-700 mb-2">Nombre Completo *</label><input type="text" value={registerData.nombre} onChange={(e) => setRegisterData({ ...registerData, nombre: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" /></div>
                            <div><label className="block text-sm font-medium text-gray-700 mb-2">Email *</label><input type="email" value={registerData.email} onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" /></div>
                            <div><label className="block text-sm font-medium text-gray-700 mb-2">Contraseña *</label><input type="password" value={registerData.password} onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" /></div>
                            <div className="grid grid-cols-2 gap-4">
                                <div><label className="block text-sm font-medium text-gray-700 mb-2">Habitación *</label><input type="number" value={registerData.habitacion} onChange={(e) => setRegisterData({ ...registerData, habitacion: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" min="1" /></div>
                                <div><label className="block text-sm font-medium text-gray-700 mb-2">Teléfono</label><input type="number" value={registerData.telefono} onChange={(e) => setRegisterData({ ...registerData, telefono: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" /></div>
                            </div>
                            <div><label className="block text-sm font-medium text-gray-700 mb-2">Fecha de Nacimiento *</label><input type="date" value={registerData.nacimiento} onChange={(e) => setRegisterData({ ...registerData, nacimiento: e.target.value })} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" max={new Date().toISOString().split('T')[0]} /></div>
                            <button onClick={handleRegister} className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition">Crear Cuenta</button>
                        </div>
                    )}
                </div>
                <p className="text-center text-gray-600 text-sm mt-6">Roomify - Sistema de Gestión de Salas</p>
            </div>
        </div>
    );
}

const ModalReserva = ({ sala, onClose, onSubmit }) => {
    // Calculamos la hora actual para el valor por defecto
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    const defaultStart = now.toISOString().slice(0, 16);

    const [formData, setFormData] = useState({
        horaInicio: defaultStart,
        observaciones: ''
    });

    const calcularHoraFin = () => {
        if (!formData.horaInicio) return '--:--';
        const start = new Date(formData.horaInicio);
        const end = new Date(start.getTime() + 3 * 60 * 60 * 1000); // +3 horas en milisegundos
        return end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-fade-in">
                <div className="bg-indigo-600 p-4 flex justify-between items-center text-white">
                    <h3 className="font-bold text-lg flex items-center gap-2">
                        <Calendar className="w-5 h-5" />
                        Reservar Sala
                    </h3>
                    <button onClick={onClose} className="hover:bg-indigo-700 p-1 rounded transition">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <div className="p-6">
                    <div className="mb-4 bg-indigo-50 p-3 rounded-lg border border-indigo-100">
                        <p className="text-sm text-indigo-800 font-semibold">{sala.descripcion}</p>
                        <p className="text-xs text-indigo-600">Aforo: {sala.aforo} personas</p>
                    </div>

                    <form onSubmit={(e) => onSubmit(e, formData)}>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Hora de Inicio</label>
                                <input
                                    type="datetime-local"
                                    required
                                    value={formData.horaInicio}
                                    onChange={(e) => setFormData({...formData, horaInicio: e.target.value})}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                                />
                            </div>
                            <div className="bg-gray-50 p-3 rounded-lg border border-gray-200 flex justify-between items-center">
                                <div>
                                    <p className="text-xs text-gray-500 font-medium uppercase">Duración Fija</p>
                                    <p className="text-sm font-bold text-gray-800">3 Horas</p>
                                </div>
                                <div className="text-right">
                                    <p className="text-xs text-gray-500 font-medium uppercase">Hora Fin Estimada</p>
                                    <p className="text-sm font-bold text-indigo-600">{calcularHoraFin()}</p>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Observaciones</label>
                                <textarea
                                    rows="3"
                                    value={formData.observaciones}
                                    onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                                    placeholder="Ej: Reunión de equipo..."
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                                />
                            </div>

                            <div className="pt-2 flex gap-3">
                                <button
                                    type="button"
                                    onClick={onClose}
                                    className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition font-medium"
                                >
                                    Confirmar Reserva
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default App;