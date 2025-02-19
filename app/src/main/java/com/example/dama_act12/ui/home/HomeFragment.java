package com.example.dama_act12.ui.home;

import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.dama_act12.AdminSQLiteOpenHelper;
import com.example.dama_act12.R;
import com.example.dama_act12.databinding.FragmentHomeBinding;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Verificar permisos necesarios
        if (getContext().checkSelfPermission(android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.INTERNET}, 1);
        }
        if (getContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        // Referencia a los elementos del layout
        EditText editTextNombre = view.findViewById(R.id.editText);
        RadioGroup radioGroupSexo = view.findViewById(R.id.radioGroupSexo);
        EditText editTextEdad = view.findViewById(R.id.editText2);
        EditText editTextDireccion = view.findViewById(R.id.editText3);
        Button btnAgregar = view.findViewById(R.id.agregar);

        // Listener para el botón "Agregar"
        btnAgregar.setOnClickListener(v -> {
            // Obtención de los valores
            String nombre = editTextNombre.getText().toString().trim();
            String sexo = radioGroupSexo.getCheckedRadioButtonId() == R.id.radioButtonM ? "Masculino" : "Femenino";
            String edadStr = editTextEdad.getText().toString().trim();
            String direccion = editTextDireccion.getText().toString().trim();

            // Validación de los datos
            if (nombre.isEmpty() || edadStr.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            int edad = Integer.parseInt(edadStr);

            // Uso de Geocoder para obtener las coordenadas
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            double latitud = 0.0;
            double longitud = 0.0;

            try {
                List<Address> addresses = geocoder.getFromLocationName(direccion, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    latitud = address.getLatitude();
                    longitud = address.getLongitude();
                } else {
                    Toast.makeText(getContext(), "Dirección no encontrada. Verifica la dirección ingresada.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al obtener coordenadas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Inserción de datos en la base de datos
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(getContext(), "personasDB", null, 1);
            SQLiteDatabase db = admin.getWritableDatabase();

            try {
                db.execSQL("INSERT INTO personas (personaID, nombre, edad, sexo, direccion, longitud, latitud) VALUES (NULL, ?, ?, ?, ?, ?, ?)",
                        new Object[]{nombre, edad, sexo, direccion, longitud, latitud});
                Toast.makeText(getContext(), "Persona añadida correctamente", Toast.LENGTH_SHORT).show();

                // Limpia los campos después de guardar
                editTextNombre.setText("");
                radioGroupSexo.clearCheck();
                editTextEdad.setText("");
                editTextDireccion.setText("");
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                db.close();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

