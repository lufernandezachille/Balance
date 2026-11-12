// URL base de la API (Backend)
const API_URL = "http://localhost:3000/api"; 

// ---------- 1. REGISTRO (CREAR CUENTA) ----------
const formularioRegistro = document.getElementById("formulario-registro");

if (formularioRegistro) {
    formularioRegistro.addEventListener("submit", async function(event) {
        event.preventDefault();

        const datosRegistro = {
            nombre: document.getElementById("nombre").value.trim(),
            apellido: document.getElementById("apellido").value.trim(),
            usuario: document.getElementById("usuario").value.trim(),
            correo: document.getElementById("correo").value.trim().toLowerCase(),
            contrasena: document.getElementById("contrasena").value,
            moneda: document.getElementById("moneda").value
        };

        const confirmar = document.getElementById("confirmar-contrasena").value;
        if (datosRegistro.contrasena !== confirmar) {
            alert("Las contraseñas no coinciden.");
            return;
        }

        try {
            const respuesta = await fetch(`${API_URL}/auth/registro`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
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
            const respuesta = await fetch(`${API_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(credenciales)
            });

            const resultado = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(resultado.mensaje || "Credenciales incorrectas.");
            }

            // Guardar Token JWT devuelto por el Backend
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
    
    // Cargar datos desde el Backend al entrar a la pantalla
    async function cargarPerfil() {
        const token = localStorage.getItem("authToken");

        if (!token) {
            alert("Tenés que iniciar sesión para acceder a tu cuenta.");
            window.location.href = "iniciarsesion.html";
            return;
        }

        try {
            const respuesta = await fetch(`${API_URL}/perfil`, {
                method: "GET",
                headers: { "Authorization": `Bearer ${token}` }
            });

            if (!respuesta.ok) throw new Error("Sesión expirada o inválida.");

            const usuario = await respuesta.json();

            // Rellenar campos del formulario
            document.getElementById("nombre").value = usuario.nombre || "";
            document.getElementById("apellido").value = usuario.apellido || "";
            document.getElementById("usuario").value = usuario.usuario || "";
            document.getElementById("correo").value = usuario.correo || "";
            document.getElementById("moneda").value = usuario.moneda || "ARS";
            
            // Actualizar encabezado del perfil
            const nombrePerfil = document.getElementById("nombre-perfil");
            const usuarioPerfil = document.getElementById("usuario-perfil");
            
            if (nombrePerfil) nombrePerfil.textContent = `${usuario.nombre} ${usuario.apellido}`;
            if (usuarioPerfil) usuarioPerfil.textContent = `@${usuario.usuario}`;
            
            // Actualizar interfaz del Plan
            actualizarInterfazPlan(usuario.plan || "Gratuito");

        } catch (error) {
            alert(error.message);
            localStorage.removeItem("authToken");
            window.location.href = "iniciarsesion.html";
        }
    }

    // Enviar cambios del formulario al Backend
    formularioCuenta.addEventListener("submit", async function(event) {
        event.preventDefault();
        const token = localStorage.getItem("authToken");

        const datosActualizados = {
            nombre: document.getElementById("nombre").value.trim(),
            apellido: document.getElementById("apellido").value.trim(),
            usuario: document.getElementById("usuario").value.trim(),
            correo: document.getElementById("correo").value.trim().toLowerCase(),
            moneda: document.getElementById("moneda").value
        };

        try {
            const respuesta = await fetch(`${API_URL}/perfil`, {
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
            cargarPerfil();

        } catch (error) {
            alert(error.message);
        }
    });

    // Ejecutar carga al entrar
    cargarPerfil();
}


// ---------- 5. ACCIONES DE BOTONES DE SUSCRIPCIÓN ----------
const btnObtener = document.getElementById("boton-obtener-premium");
const btnCancelar = document.getElementById("boton-cancelar-premium");

// Redirección a la pantalla de compra Premium
if (btnObtener) {
    btnObtener.addEventListener("click", () => {
        window.location.href = "planpremium.html";
    });
}

// Acción para cancelar suscripción y volver al plan Gratuito
if (btnCancelar) {
    btnCancelar.addEventListener("click", async () => {
        if (confirm("¿Estás seguro/a de que querés cancelar tu suscripción Premium?")) {
            const token = localStorage.getItem("authToken");

            try {
                const respuesta = await fetch(`${API_URL}/perfil/plan`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`
                    },
                    body: JSON.stringify({ plan: "Gratuito" })
                });

                if (!respuesta.ok) throw new Error("No se pudo actualizar el plan.");

                alert("Has vuelto al plan Gratuito.");
                actualizarInterfazPlan("Gratuito");

            } catch (error) {
                alert(error.message);
            }
        }
    });
}


// ---------- 6. CERRAR SESIÓN ----------
const botonCerrarSesion = document.getElementById("boton-cerrar-sesion");

if (botonCerrarSesion) {
    botonCerrarSesion.addEventListener("click", function() {
        if (confirm("¿Estás seguro de que querés cerrar sesión?")) {
            localStorage.removeItem("authToken");
            window.location.href = "iniciarsesion.html";
        }
    });
}