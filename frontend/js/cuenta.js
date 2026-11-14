
// URL base de la API (Backend)
const API_URL = "http://localhost:8080/api";


// ---------- 1. REGISTRO (CREAR CUENTA) ----------

const formularioRegistro = document.getElementById("formulario-registro");

if (formularioRegistro) {
    formularioRegistro.addEventListener("submit", async function(event) {
        event.preventDefault();

        const datosRegistro = {
            nombre: document.getElementById("nombre").value.trim(),
            apellido: document.getElementById("apellido").value.trim(),
            nombreUsuario: document.getElementById("usuario").value.trim(),
            correo: document.getElementById("correo").value.trim().toLowerCase(),
            contrasena: document.getElementById("contrasena").value,
            codigoMoneda: document.getElementById("moneda").value
        };

        const confirmar = document.getElementById("confirmar-contrasena").value;

        if (datosRegistro.contrasena !== confirmar) {
            alert("Las contraseñas no coinciden.");
            return;
        }

        try {
            const respuesta = await fetch(`${API_URL}/usuarios/registro`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datosRegistro)
            });

            const resultado = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(resultado.mensaje || "Error al crear la cuenta.");
            }

            alert("¡Cuenta creada correctamente!");
            window.location.href = "iniciarsesion.html";

        } catch (error) {
            alert(error.message);
        }
    });
}


// ---------- 2. INICIAR SESIÓN ----------

const formularioLogin = document.getElementById("formulario-login");

if (formularioLogin) {
    formularioLogin.addEventListener("submit", async function(event) {
        event.preventDefault();

        const credenciales = {
            correo: document.getElementById("correo").value.trim().toLowerCase(),
            contrasena: document.getElementById("contrasena").value
        };

        try {
            const respuesta = await fetch(`${API_URL}/usuarios/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(credenciales)
            });

            const resultado = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(resultado.mensaje || "Credenciales incorrectas.");
            }

            localStorage.setItem("authToken", resultado.token);

            alert("¡Bienvenido/a a Balance!");
            window.location.href = "../index.html";

        } catch (error) {
            alert(error.message);
        }
    });
}


// ---------- 3. AUXILIAR: ACTUALIZAR INTERFAZ DEL PLAN ----------

function actualizarInterfazPlan(planActual) {
    const textoPlan = document.getElementById("texto-plan-actual");
    const badgePlan = document.getElementById("badge-plan");
    const btnObtener = document.getElementById("boton-obtener-premium");
    const btnCancelar = document.getElementById("boton-cancelar-premium");

    if (textoPlan) textoPlan.textContent = planActual;

    if (badgePlan) {
        badgePlan.textContent = `Plan ${planActual}`;
    }

    if (planActual === "Premium") {
        if (btnObtener) btnObtener.hidden = true;
        if (btnCancelar) btnCancelar.hidden = false;
        if (badgePlan) badgePlan.classList.add("premium");
    } else {
        if (btnObtener) btnObtener.hidden = false;
        if (btnCancelar) btnCancelar.hidden = true;
        if (badgePlan) badgePlan.classList.remove("premium");
    }
}


// ---------- 4. CARGAR Y GUARDAR PERFIL ----------

const formularioCuenta = document.getElementById("formulario-cuenta");

if (formularioCuenta) {

    const vistaInformacion = document.getElementById("vista-informacion");

    // Cargar datos del usuario desde el Backend
    async function cargarPerfil() {
        const token = localStorage.getItem("authToken");

        if (!token) {
            alert("Tenés que iniciar sesión para acceder a tu cuenta.");
            window.location.href = "iniciarsesion.html";
            return;
        }

        try {
            const respuesta = await fetch(`${API_URL}/usuarios/perfil`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });

            if (!respuesta.ok) {
                throw new Error("Sesión expirada o inválida.");
            }

            const usuario = await respuesta.json();

            // Completar formulario
            document.getElementById("nombre").value = usuario.nombre || "";
            document.getElementById("apellido").value = usuario.apellido || "";
            document.getElementById("usuario").value = usuario.nombreUsuario || "";
            document.getElementById("correo").value = usuario.correo || "";
            document.getElementById("moneda").value = usuario.codigoMoneda || "ARS";

            // Actualizar encabezado
            const nombrePerfil = document.getElementById("nombre-perfil");
            const usuarioPerfil = document.getElementById("usuario-perfil");

            if (nombrePerfil) {
                nombrePerfil.textContent = `${usuario.nombre} ${usuario.apellido}`;
            }

            if (usuarioPerfil) {
                usuarioPerfil.textContent = `@${usuario.nombreUsuario}`;
            }

            // Actualizar vista de solo lectura
            document.getElementById("vista-nombre-completo").textContent =
                `${usuario.nombre} ${usuario.apellido}`;

            document.getElementById("vista-usuario").textContent =
                `@${usuario.nombreUsuario}`;

            document.getElementById("vista-correo").textContent =
                usuario.correo;

            document.getElementById("vista-moneda").textContent =
                usuario.codigoMoneda;

        } catch (error) {
            alert(error.message);
            localStorage.removeItem("authToken");
            window.location.href = "iniciarsesion.html";
        }
    }

    // Cargar suscripción actual desde el Backend
    async function cargarSuscripcion() {

        const token = localStorage.getItem("authToken");

        try {

            const respuesta = await fetch(`${API_URL}/usuarios/suscripcion`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });

            if (!respuesta.ok) {
                throw new Error("No se pudo obtener la suscripción.");
            }

            const suscripcion = await respuesta.json();

            const nombrePlan = suscripcion.nombrePlan;

            if (nombrePlan === "PREMIUM") {
                actualizarInterfazPlan("Premium");
            } else {
                actualizarInterfazPlan("Gratis");
            }

        } catch (error) {

            console.error("Error al cargar la suscripción:", error);

        }
    }

    // Guardar cambios del perfil
    formularioCuenta.addEventListener("submit", async function(event) {
        event.preventDefault();

        const token = localStorage.getItem("authToken");

        const datosActualizados = {
            nombre: document.getElementById("nombre").value.trim(),
            apellido: document.getElementById("apellido").value.trim(),
            nombreUsuario: document.getElementById("usuario").value.trim(),
            correo: document.getElementById("correo").value.trim().toLowerCase(),
            codigoMoneda: document.getElementById("moneda").value
        };

        try {
            const respuesta = await fetch(`${API_URL}/usuarios/perfil`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(datosActualizados)
            });

            const resultado = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(resultado.mensaje || "Error al actualizar los datos.");
            }

            alert("¡Tus datos se actualizaron correctamente!");

            await cargarPerfil();

            formularioCuenta.hidden = true;
            vistaInformacion.hidden = false;

        } catch (error) {
            alert(error.message);
        }
    });


    // Activar edición
    const botonActivarEdicion = document.getElementById("boton-activar-edicion");

    if (botonActivarEdicion) {
        botonActivarEdicion.addEventListener("click", () => {
            vistaInformacion.hidden = true;
            formularioCuenta.hidden = false;
        });
    }


    // Cancelar edición
    const botonCancelarEdicion = document.getElementById("boton-cancelar-edicion");

    if (botonCancelarEdicion) {
        botonCancelarEdicion.addEventListener("click", async () => {
            await cargarPerfil();

            formularioCuenta.hidden = true;
            vistaInformacion.hidden = false;
        });
    }

    // Cargar perfil y suscripción al entrar
    cargarPerfil();
    cargarSuscripcion();
}


// ---------- 5. ACCIONES DE BOTONES DE SUSCRIPCIÓN ----------

const btnObtener = document.getElementById("boton-obtener-premium");

if (btnObtener) {
    btnObtener.addEventListener("click", () => {
        window.location.href = "planpremium.html";
    });
}


// ---------- 6. CERRAR SESIÓN ----------

const botonCerrarSesion = document.getElementById("boton-cerrar-sesion");

if (botonCerrarSesion) {
    botonCerrarSesion.addEventListener("click", async function() {

        if (confirm("¿Estás seguro de que querés cerrar sesión?")) {

            const token = localStorage.getItem("authToken");

            try {
                const respuesta = await fetch(`${API_URL}/usuarios/logout`, {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                });

                if (!respuesta.ok) {
                    throw new Error("No se pudo cerrar la sesión en el servidor.");
                }

            } catch (error) {
                alert(error.message);

            } finally {
                localStorage.removeItem("authToken");
                window.location.href = "iniciarsesion.html";
            }
        }
    });
}

