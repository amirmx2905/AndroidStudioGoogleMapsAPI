package com.example.dama_act12.ui.gallery;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.dama_act12.AdminSQLiteOpenHelper;
import com.example.dama_act12.R;
import com.example.dama_act12.databinding.FragmentGalleryBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class GalleryFragment extends Fragment implements OnMapReadyCallback {

    private FragmentGalleryBinding binding;
    Bitmap originalBitmap, resizedBitmap;
    private Spinner mapTypeSpinner;
    private GoogleMap mGoogleMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtén el fragmento del mapa y configura el callback
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar los Spinner
        mapTypeSpinner = view.findViewById(R.id.spinner_map_type);
        Spinner genderFilterSpinner = view.findViewById(R.id.spinner_gender_filter);

        // Adaptador para el Spinner de tipo de mapa
        ArrayAdapter<CharSequence> mapTypeAdapter = new ArrayAdapter<CharSequence>(getContext(),
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.map_types)) {

            // Personalizar la vista del ítem seleccionado
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Centrar el texto y cambiar el color
                textView.setTextColor(getResources().getColor(R.color.spinner_text_color));
                textView.setGravity(Gravity.CENTER); // Centrar el texto

                return view;
            }

            // Personalizar la vista de cada ítem del dropdown
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Centrar el texto y cambiar el color
                textView.setTextColor(getResources().getColor(R.color.spinner_text_color));
                textView.setGravity(Gravity.CENTER); // Centrar el texto

                // Establecer el fondo para cada ítem del dropdown
                view.setBackgroundColor(getResources().getColor(R.color.spinner_dropdown_background));

                return view;
            }
        };
        mapTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(mapTypeAdapter);

        // Adaptador para el Spinner de filtro de género (con sus propias opciones)
        ArrayAdapter<CharSequence> genderFilterAdapter = new ArrayAdapter<CharSequence>(getContext(),
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.gender_filter_options)) {

            // Personalizar la vista del ítem seleccionado
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Centrar el texto y cambiar el color
                textView.setTextColor(getResources().getColor(R.color.spinner_text_color));
                textView.setGravity(Gravity.CENTER); // Centrar el texto

                return view;
            }

            // Personalizar la vista de cada ítem del dropdown
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Centrar el texto y cambiar el color
                textView.setTextColor(getResources().getColor(R.color.spinner_text_color));
                textView.setGravity(Gravity.CENTER); // Centrar el texto

                // Establecer el fondo para cada ítem del dropdown
                view.setBackgroundColor(getResources().getColor(R.color.spinner_dropdown_background));

                return view;
            }
        };
        genderFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderFilterSpinner.setAdapter(genderFilterAdapter);

        // Listener para detectar cambios en la selección del Spinner de tipo de mapa
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mGoogleMap != null) {
                    switch (position) {
                        case 0: // Vista predeterminada
                            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1: // Vista satelital
                            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2: // Vista relieve
                            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Si no se selecciona nada, no hacemos nada
            }
        });

        // Listener para detectar cambios en la selección del Spinner de filtro de género
        genderFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mGoogleMap != null) {
                    // Llamar a la función de filtrado de marcadores
                    filterMarkers(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mGoogleMap = googleMap;

        View zoomInButton = getView().findViewById(R.id.btnZoomIn);
        View zoomOutButton = getView().findViewById(R.id.btnZoomOut);

        zoomInButton.setOnClickListener(v -> {
            float currentZoom = googleMap.getCameraPosition().zoom;
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1));
        });

        zoomOutButton.setOnClickListener(v -> {
            float currentZoom = googleMap.getCameraPosition().zoom;
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1));
        });
    }

    private void filterMarkers(int filterPosition) {
        // Limpiar los marcadores actuales
        mGoogleMap.clear();

        // Obtener datos de la base de datos y aplicar filtro
        AdminSQLiteOpenHelper dbHelper = new AdminSQLiteOpenHelper(getContext(), "personasDB", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.obtenerPersonas(db);

        int personaIDIndex = cursor.getColumnIndex("personaID");
        int nombreIndex = cursor.getColumnIndex("nombre");
        int sexoIndex = cursor.getColumnIndex("sexo");
        int edadIndex = cursor.getColumnIndex("edad");
        int latitudIndex = cursor.getColumnIndex("latitud");
        int longitudIndex = cursor.getColumnIndex("longitud");

        if (cursor.moveToFirst()) {
            do {
                String sexo = cursor.getString(sexoIndex);

                // Si "Todos" es seleccionado, mostramos todos los marcadores
                if (filterPosition == 0 || (filterPosition == 1 && sexo.equals("Masculino")) || (filterPosition == 2 && sexo.equals("Femenino"))) {
                    // Obtener los datos y agregar el marcador
                    int personaID = cursor.getInt(personaIDIndex);
                    String nombre = cursor.getString(nombreIndex);
                    int edad = cursor.getInt(edadIndex);
                    double latitud = cursor.getDouble(latitudIndex);
                    double longitud = cursor.getDouble(longitudIndex);
                    LatLng posicion = new LatLng(latitud, longitud);

                    if ( edad >= 0 && edad <= 17) {
                        if (sexo.equals("Masculino")) {
                            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boy);

                            int width = 90;  // Ancho del pin en píxeles
                            int height = 90; // Alto del pin en píxeles
                            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));
                        } else if (sexo.equals("Femenino")){
                            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.girl);

                            int width = 160;  // Ancho del pin en píxeles
                            int height = 160; // Alto del pin en píxeles
                            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));
                        }
                    } else if (edad >= 18 && edad <= 60) {
                        if (sexo.equals("Masculino")) {
                            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.man);

                            int width = 130;  // Ancho del pin en píxeles
                            int height = 130; // Alto del pin en píxeles
                            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));
                        } else if (sexo.equals("Femenino")){
                            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.woman);

                            int width = 130;  // Ancho del pin en píxeles
                            int height = 130; // Alto del pin en píxeles
                            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));
                        }

                    } else if (edad > 60) {
                        if (sexo.equals("Masculino")) {
                            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grandpa);

                            int width = 130;  // Ancho del pin en píxeles
                            int height = 130; // Alto del pin en píxeles
                            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));

                        } else if (sexo.equals("Femenino")){
                            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.granny);

                            int width = 130;  // Ancho del pin en píxeles
                            int height = 130; // Alto del pin en píxeles
                            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));
                        }

                    } // Iconos edades y sexos
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}