
// ---------- CREAR CUENTA ----------

const formularioRegistro = document.getElementById("formulario-registro");

if (formularioRegistro) {
    formularioRegistro.addEventListener("submit", function(event) {
        event.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const apellido = document.getElementById("apellido").value.trim();
        const usuario = document.getElementById("usuario").value.trim();
        const correo = document.getElementById("correo").value.trim().toLowerCase();
        const contrasena = document.getElementById("contrasena").value;
        const confirmar = document.getElementById("confirmar-contrasena").value;
        const moneda = document.getElementById("moneda").value;

        // Comprobar que las contraseñas coincidan
        if (contrasena !== confirmar) {
            alert("Las contraseñas no coinciden.");
            return;
        }

        // Obtener las cuentas guardadas
        const cuentas = JSON.parse(localStorage.getItem("cuentas")) || [];

        // Comprobar si el correo ya está registrado
        const existe = cuentas.some(function(cuenta) {
            return cuenta.correo === correo;
        });

        if (existe) {
            alert("Ya existe una cuenta con ese correo electrónico.");
            return;
        }

        // Crear la nueva cuenta
        const nuevaCuenta = {
            nombre: nombre,
            apellido: apellido,
            usuario: usuario,
            correo: correo,
            contrasena: contrasena,
            moneda: moneda
        };

        cuentas.push(nuevaCuenta);

        // Guardar las cuentas
        localStorage.setItem("cuentas", JSON.stringify(cuentas));

        alert("¡Cuenta creada correctamente!");

        // Ir a iniciar sesión
        window.location.href = "iniciarsesion.html";

    });

}


// ---------- INICIAR SESIÓN ----------

const formularioLogin = document.getElementById("formulario-login");

if (formularioLogin) {

    formularioLogin.addEventListener("submit", function(event) {

        event.preventDefault();

        const correo = document.getElementById("correo").value.trim().toLowerCase();
        const contrasena = document.getElementById("contrasena").value;

        const cuentas = JSON.parse(localStorage.getItem("cuentas")) || [];

        // Buscar una cuenta con ese correo y contraseña
        const cuenta = cuentas.find(function(cuenta) {
            return cuenta.correo === correo &&
                   cuenta.contrasena === contrasena;
        });

        if (!cuenta) {
            alert("El correo o la contraseña son incorrectos.");
            return;
        }

        // Guardar qué usuario inició sesión
        localStorage.setItem("usuarioActivo", cuenta.correo);

        alert("¡Bienvenido/a a Balance!");

        // Ir a Inicio
        window.location.href = "../index.html";

    });

}


// ---------- CARGAR DATOS DE MI CUENTA ----------

const formularioCuenta = document.getElementById("formulario-cuenta");

if (formularioCuenta) {

    const correoActivo = localStorage.getItem("usuarioActivo");
    const cuentas = JSON.parse(localStorage.getItem("cuentas")) || [];

    const cuenta = cuentas.find(function(cuenta) {
        return cuenta.correo === correoActivo;
    });

    if (!cuenta) {

        alert("Tenés que iniciar sesión para acceder a tu cuenta.");
        window.location.href = "cuenta.html";

    } else {

        // Mostrar los datos guardados
        document.getElementById("nombre").value = cuenta.nombre;
        document.getElementById("apellido").value = cuenta.apellido;
        document.getElementById("usuario").value = cuenta.usuario;
        document.getElementById("correo").value = cuenta.correo;
        document.getElementById("moneda").value = cuenta.moneda;
        document.getElementById("nombre-perfil").textContent = cuenta.nombre + " " + cuenta.apellido;
        document.getElementById("usuario-perfil").textContent = "@" + cuenta.usuario;
    }


    // ---------- EDITAR PERFIL ----------

    const botonEditar = document.getElementById("boton-editar");
    const botonesEdicion = document.getElementById("botones-edicion");
    const botonCancelar = document.getElementById("boton-cancelar");

    function cambiarEdicion(editar) {

        document.getElementById("nombre").readOnly = !editar;
        document.getElementById("apellido").readOnly = !editar;
        document.getElementById("usuario").readOnly = !editar;
        document.getElementById("correo").readOnly = !editar;

        document.getElementById("moneda").disabled = !editar;

        botonEditar.hidden = editar;
        botonesEdicion.hidden = !editar;

    }


    botonEditar.addEventListener("click", function() {
        cambiarEdicion(true);
    });


    // Cancelar los cambios
    botonCancelar.addEventListener("click", function() {

        document.getElementById("nombre").value = cuenta.nombre;
        document.getElementById("apellido").value = cuenta.apellido;
        document.getElementById("usuario").value = cuenta.usuario;
        document.getElementById("correo").value = cuenta.correo;
        document.getElementById("moneda").value = cuenta.moneda;

        cambiarEdicion(false);

    });


    // Guardar los cambios
    formularioCuenta.addEventListener("submit", function(event) {

        event.preventDefault();

        const nuevoCorreo = document.getElementById("correo").value.trim().toLowerCase();

        // Comprobar que otro usuario no tenga ese correo
        const correoExiste = cuentas.some(function(otraCuenta) {
            return otraCuenta.correo === nuevoCorreo &&
                   otraCuenta.correo !== cuenta.correo;
        });

        if (correoExiste) {
            alert("Ese correo ya está registrado.");
            return;
        }

        // Actualizar los datos
        cuenta.nombre = document.getElementById("nombre").value.trim();
        cuenta.apellido = document.getElementById("apellido").value.trim();
        cuenta.usuario = document.getElementById("usuario").value.trim();
        cuenta.correo = nuevoCorreo;
        cuenta.moneda = document.getElementById("moneda").value;
        document.getElementById("nombre-perfil").textContent = cuenta.nombre + " " + cuenta.apellido;
    document.getElementById("usuario-perfil").textContent = "@" + cuenta.usuario;

        // Guardar la lista actualizada
        localStorage.setItem("cuentas", JSON.stringify(cuentas));

        // Actualizar el usuario activo si cambió el correo
        localStorage.setItem("usuarioActivo", cuenta.correo);

        alert("¡Tus datos se actualizaron correctamente!");

        cambiarEdicion(false);

    });

}


// ---------- CERRAR SESIÓN ----------

const botonCerrarSesion = document.getElementById("boton-cerrar-sesion");

if (botonCerrarSesion) {

    botonCerrarSesion.addEventListener("click", function() {

        const confirmar = confirm("¿Estás seguro de que querés cerrar sesión?");

        if (confirmar) {

            localStorage.removeItem("usuarioActivo");

            window.location.href = "cuenta.html";

        }

    });

}
