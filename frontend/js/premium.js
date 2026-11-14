const API_URL = "http://localhost:8080/api";

async function cargarPrecios() {

    try {

        const respuesta = await fetch(`${API_URL}/planes/premium`);

        if (!respuesta.ok) {
            throw new Error("No se pudieron obtener los precios");
        }

        const plan = await respuesta.json();

        const formatoPrecio = new Intl.NumberFormat("es-AR", {
            style: "currency",
            currency: "ARS",
            maximumFractionDigits: 0
        });

        document.getElementById("precio-mensual").textContent =
            `${formatoPrecio.format(plan.precioMensual)} / mes`;

        document.getElementById("precio-anual").textContent =
            `${formatoPrecio.format(plan.precioAnual)} / año`;

    } catch (error) {

        console.error("Error al cargar los precios:", error);

        document.getElementById("precio-mensual").textContent =
            "No disponible";

        document.getElementById("precio-anual").textContent =
            "No disponible";
    }
}

cargarPrecios();

let frecuenciaSeleccionada = null;

const botonMensual = document.getElementById("boton-mensual");
const botonAnual = document.getElementById("boton-anual");
const botonGuardar = document.getElementById("boton-guardar-plan");

// Seleccionar plan mensual
botonMensual.addEventListener("click", () => {

    frecuenciaSeleccionada = "MENSUAL";

    botonMensual.classList.add("activo");
    botonAnual.classList.remove("activo");

    botonGuardar.disabled = false;
});

// Seleccionar plan anual
botonAnual.addEventListener("click", () => {

    frecuenciaSeleccionada = "ANUAL";

    botonAnual.classList.add("activo");
    botonMensual.classList.remove("activo");

    botonGuardar.disabled = false;
});

// Guardar suscripción
botonGuardar.addEventListener("click", async () => {

    const token = localStorage.getItem("token");

    if (!token) {
        alert("Tenés que iniciar sesión para cambiar tu plan.");
        window.location.href = "iniciarsesion.html";
        return;
    }

    if (!frecuenciaSeleccionada) {
        alert("Primero seleccioná una opción.");
        return;
    }

    botonGuardar.disabled = true;

    try {

        const respuesta = await fetch(`${API_URL}/usuarios/suscripcion/premium`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                frecuenciaPago: frecuenciaSeleccionada
            })
        });

        if (!respuesta.ok) {
            throw new Error("No se pudo actualizar la suscripción.");
        }

        alert("¡Tu plan Premium se activó correctamente!");

        window.location.href = "configuracion.html";

    } catch (error) {

        console.error("Error al activar Premium:", error);
        alert("Ocurrió un error al guardar el cambio. Intentá nuevamente.");
        botonGuardar.disabled = false;
    }
});
